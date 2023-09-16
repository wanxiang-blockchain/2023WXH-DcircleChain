package com.base.foundation.oss

import android.util.Log
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask
import com.alibaba.sdk.android.oss.model.ResumableDownloadRequest
import com.alibaba.sdk.android.oss.model.ResumableDownloadResult
import com.base.foundation.DCircleScope
import com.base.foundation.baselib.UserSpace
import com.base.foundation.db.*
import com.base.foundation.getUs
import com.base.foundation.nc.ObserverAble
import com.base.foundation.oss.OSSNetDownloader.Companion.sharedFlow
import com.base.foundation.oss.OSSNetDownloader.Companion.wait
import com.base.foundation.oss.OSSNetDownloader.Companion.waitLocker
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.NetworkUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.sync.Mutex
import java.io.File
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max

interface OSSNetDownloaderGetter {
	suspend fun AddTask(task:DownloadTask):Error?
	suspend fun AddTask(task:MutableList<DownloadTask>):Error?
	fun Close()
	suspend fun Start(us:UserSpace)
}

open class OSSNetDownloaderFactory(private var us: UserSpace) : OSSNetDownloaderGetter,
	NetworkUtils.OnNetworkStatusChangedListener {
	private val downloaders = mutableListOf(
		OSSNetDownloader(us,Priority.Channel1),
		OSSNetDownloader(us,Priority.Channel4),
	)

	override suspend fun AddTask(task: DownloadTask): Error? {
		val errors: List<Error?> = coroutineScope {
			downloaders.map {
				async { it.AddTask(task) }
			}.awaitAll()
		}

		return errors.filterNotNull().firstOrNull()
	}

	override suspend fun AddTask(task: MutableList<DownloadTask>): Error? {
		val errors: List<Error?> = coroutineScope {
			task.map { task ->
				async { AddTask(task) }
			}.awaitAll()
		}

		return errors.filterNotNull().firstOrNull()
	}

	override fun Close() {
		NetworkUtils.unregisterNetworkStatusChangedListener(this)
		for (entry in downloaders) {
			entry.Close()
		}
	}

	override suspend fun Start(us: UserSpace) {
		NetworkUtils.registerNetworkStatusChangedListener(this)
		DownloadTask.Delete(us)
		for (entry in downloaders) {
			entry.Start()
		}
	}

	override fun onDisconnected() {
	}

	override fun onConnected(networkType: NetworkUtils.NetworkType?) {
		DCircleScope.launch {
			DownloadTask.FindDownloadingTask().firstOrNull()?.apply {
				AddTask(this)
			}
		}
	}
}

class OSSNetDownloader(private var us:UserSpace, var priority: Priority):ObserverAble {
	private val scopes:MutableList<CoroutineScope> = mutableListOf()
	init {
		for (i in 0 until priority.value) {
			scopes.add(CoroutineScope(Dispatchers.IO))
		}
	}

	companion object {
		internal val locker:Mutex = Mutex()
		internal val downloading:MutableList<String> = mutableListOf()
		internal val sharedFlow = MutableSharedFlow<String>(replay = Int.MAX_VALUE)
		internal val wait:MutableMap<String, MutableList<Channel<Error?>>> = mutableMapOf()
		internal val waitLocker:Mutex = Mutex()
	}

	fun name():String {
		return "${us.getUid()}_${OSSNetDownloader::class.java.simpleName}_priority${priority.value}"
	}
	suspend fun PostProgressEvent(task: DownloadTask) {
		val objectId = task.Id
		val event = DownloadTask.ProgressEvent(objectId)
        event.ids = listOf(objectId)
		getUs().nc.postToMain(event)
	}


	suspend fun PostSuccessEvent(task:DownloadTask) {
		Log.d(this@OSSNetDownloader::class.java.simpleName, "${name()} [END] task objectId=${task.Id} PostSuccessEvent start")

		val objectId = task.Id
		val event = DownloadTask.SuccessEvent(objectId)
		event.ids = listOf(objectId)
		getUs().nc.postToMain(event)

		var channels:MutableList<Channel<Error?>> = mutableListOf()
		waitLocker.lock()
		channels = wait[task.Id]?: mutableListOf()
		wait[task.Id] = mutableListOf()
		waitLocker.unlock()
		for (channel in channels) {
			channel.send(null)
		}
		task.delete()
		Log.d(this@OSSNetDownloader::class.java.simpleName, "${name()} [END] task objectId=${task.Id} PostSuccessEvent end")
	}

	private suspend fun PostFailEvent(task:DownloadTask) {
		Log.d(this@OSSNetDownloader::class.java.simpleName, "${name()} [END] task objectId=${task.Id} PostFailEvent start")

		val objectId = task.Id
		val event = DownloadTask.FailEvent(objectId)
		event.ids = listOf(objectId)
		getUs().nc.postToMain(event)

		var channels:MutableList<Channel<Error?>> = mutableListOf()
		waitLocker.lock()
		channels = wait[task.Id]?: mutableListOf()
		wait[task.Id] = mutableListOf()
		waitLocker.unlock()
		for (channel in channels) {
			channel.send(null)
		}

		Log.d(this@OSSNetDownloader::class.java.simpleName, "${name()} [END] task objectId=${task.Id} PostFailEvent end")
	}

	fun Start() {
		DBLog.Insert(this@OSSNetDownloader::class.java.simpleName, "${name()} Start")
		for (scope in scopes) {
			scope.launch {
				sharedFlow
					.shareIn(this, SharingStarted.WhileSubscribed(replayExpirationMillis = 0))
					.collect { taskId ->
						Log.d(this@OSSNetDownloader::class.java.simpleName, "${name()} sharedFlow has taskId=${taskId}")

						while (isActive) {
							val lists = CopyOnWriteArrayList(downloading)
							val networkType = NetworkUtils.getNetworkType()
							val networks = if (networkType == NetworkUtils.NetworkType.NETWORK_WIFI || networkType == NetworkUtils.NetworkType.NETWORK_ETHERNET) {
								listOf(DownloadTask.NetworkType.Cellular, DownloadTask.NetworkType.WIFI)
							} else {
								listOf(DownloadTask.NetworkType.Cellular)
							}
							
							val task = DownloadTask.FindByPriority(priority, lists, networks)?: return@collect

							locker.lock()
							if (downloading.contains(taskId)) {
								locker.unlock()
								Log.w(this@OSSNetDownloader::class.java.simpleName, "${name()} [START] task objectId=${task.Id}, but other scope is uploading")
								continue
							}

							downloading.add(task.Id)
							locker.unlock()

							Log.d(this@OSSNetDownloader::class.java.simpleName, "${name()} [START] task objectId=${task.Id} downloader's priority=${priority} task's priority=${task.Priority}")

							if (GetSandboxOSSFile().has(task.Id)) {
								task.Progress = 100
								task.StatusCode = DownloadTask.Code.OK.int
								task.SetProgress()
								PostProgressEvent(task)
								PostSuccessEvent(task)
								locker.lock()
								downloading.remove(task.Id)
								locker.unlock()
								continue
							}

							val (client, bucket) = GetOSSClient()
                            if (client == null || bucket == null) {
								Log.w(this@OSSNetDownloader::class.java.simpleName, "${name()}  task objectId=${task.Id} cliet or bucket is null.")
								locker.lock()
								downloading.remove(task.Id)
								locker.unlock()
								delay(3000)
								continue
							}

							val objectId = task.Id
							val downloadToFilePath = GetSandboxOSSFile().path(objectId) + ".${OSSNetDownloader::class.java.simpleName}.tmp"
							FileUtils.createOrExistsFile(downloadToFilePath)
							val checkpointDir = GetSandboxOSSFile().path(objectId) + ".${OSSNetDownloader::class.java.simpleName}.checkpoint"
							FileUtils.createOrExistsDir(checkpointDir)
							val request = ResumableDownloadRequest(bucket.bucket, objectId, downloadToFilePath, checkpointDir)
							request.setProgressListener { _, currentSize, totalSize ->
								val percent = ((currentSize.toDouble() / totalSize.toDouble()) * 100).toInt()
								task.Progress = percent.coerceAtMost(99)
								if (task.Progress>=100) {
									Log.d(this@OSSNetDownloader::class.java.simpleName, "${name()} task objectId=${task.Id} progress=${task.Progress}")
									return@setProgressListener
								}

								if (task.SetProgressDelay()) {
									DCircleScope.launch {
										PostProgressEvent(task)
									}
								}
							}

							val wait:Channel<DownloadTask.Code> = Channel(1)
							task.SetProgress(true)
							task.LastStartTime = Date().time
							task.SetLastStartTime()
							val downloadTask: OSSAsyncTask<ResumableDownloadResult> = client.asyncResumableDownload(request,
								object : OSSCompletedCallback<ResumableDownloadRequest, ResumableDownloadResult> {
									override fun onSuccess(request: ResumableDownloadRequest?, result: ResumableDownloadResult?) {
										DCircleScope.launch {
											if (result != null && result.statusCode == DownloadTask.Code.OK.int) {
												val file = GetSandboxOSSFile().path(objectId)
												val err = GetSandboxOSSFile().mv(File(downloadToFilePath), File(file))
												if (err!=null) {
													wait.send(DownloadTask.Code.FAIL)
													return@launch
												}

												File(checkpointDir).deleteRecursively()

												task.Progress = max(100, task.Progress)
												task.SetProgress()
												wait.send(DownloadTask.Code.OK)
												return@launch
											}

											wait.send(DownloadTask.Code.FAIL)
										}
									}

									override fun onFailure(request: ResumableDownloadRequest?, clientException: ClientException?, serviceException: ServiceException?) {
										Log.d(this@OSSNetDownloader::class.java.simpleName,
											"task objectId=${task.Id} download failure serviceException=${serviceException} clientException=${clientException}")
										DCircleScope.launch {
											if (serviceException?.statusCode == 403) {
												val keyVal = KeyVal()
												keyVal.Key = KeyVal.Keys.AliyunSTSToken.toString()
												keyVal.ExpireTime = 0
												keyVal.update()
												wait.send(DownloadTask.Code.TokenExpire)
												return@launch
											}

											if (serviceException?.statusCode == 404) {
												task.StatusCode = serviceException.statusCode
												task.SetStatusCode()
											}

											wait.send(DownloadTask.Code.FAIL)
										}
									}
								})

							val code = wait.receive()

							Log.d(this@OSSNetDownloader::class.java.simpleName, "${name()} task objectId=${task.Id} code=$code")

							if (code == DownloadTask.Code.OK) {
								task.StatusCode = DownloadTask.Code.OK.int
								task.SetStatusCode()
								task.LastEndTime = Date().time
								task.SetLastEndTime()
								PostSuccessEvent(task)
							}

							if (code == DownloadTask.Code.NotFound) {
								task.StatusCode = DownloadTask.Code.NotFound.int
								task.SetStatusCode()
								PostFailEvent(task)
							}

							locker.lock()
							downloading.remove(task.Id)
							locker.unlock()
						}
					}
			}
		}
	}

	fun Close() {
		DBLog.Insert(this@OSSNetDownloader::class.java.simpleName, "${name()} Cancel")
		for (scope in scopes) {
			scope.cancel()
		}
	}
}

suspend fun OSSNetDownloader.AddTask(task: DownloadTask): Error? {
	if (task.insert() != null) {
		val doc = DownloadTask.FindById(task.Id)
		if (doc != null && doc.Progress == 100) {
			if (GetSandboxOSSFile().has(doc.Id)) {
				PostProgressEvent(doc)
				PostSuccessEvent(doc)
				return null
            }
		}
		task.SetNetworkType()
	}

	val ch = Channel<Error?>(1)
	waitLocker.lock()
	if (wait[task.Id]==null) {
		wait[task.Id] = mutableListOf()
	}
	wait[task.Id]?.add(ch)
	waitLocker.unlock()

	Log.d(OSSNetDownloader::class.java.simpleName, "${name()} [ADD] task objectId=${task.Id} downloader's priority=${priority} task's priority=${task.Priority}")
	sharedFlow.emit(task.Id)
	return ch.receive()
}