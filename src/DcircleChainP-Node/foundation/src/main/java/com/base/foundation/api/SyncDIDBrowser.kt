package com.base.foundation.api

import OkDownloader
import android.util.Log
import com.base.foundation.NetConfigBaseUrl
import com.base.foundation.api.http.OkhttpBuilderCreator
import com.base.foundation.getUs
import com.base.foundation.oss.GetSandboxDBFile
import com.blankj.utilcode.util.FileUtils
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.net.URI

class SyncDIDBrowserRequest

class SyncDIDBrowserResponse {
	var Files:MutableList<String> = mutableListOf()
	var BaseUrl:MutableList<String> = mutableListOf()
	var Version:String = ""
	@SerializedName("Entrypoint")
	var Entry:String = ""
}

suspend fun SyncDIDBrowser():Error? {
	val net = getUs().nf.get(NetConfigBaseUrl)
	net.setBaseUrl(NetConfigBaseUrl)
	net.setHttpBuilderCreator(OkhttpBuilderCreator())

	val (ret, err) = postJsonNoTokenSus<SyncDIDBrowserResponse>("/configs/dcirclescan.json", SyncDIDBrowserRequest(),
		net, SyncDIDBrowserResponse::class.java)
	if (err!=null) {
		delay(5000)
		return SyncDIDBrowser()
	}

	for (baseUrl in ret.BaseUrl) {
		if (download(baseUrl, ret.Files, ret.Entry, ret.Version)==null) {
			val from = GetSandboxDBFile().path("${ret.Version}${File.separator}${ret.Entry}")
			val to = GetSandboxDBFile().path(ret.Entry)
			FileUtils.copy(File(from), File(to))
			break
		}
	}

	getUs().nf.del(net)

	return null
}

private suspend fun download(baseUrl:String, files:MutableList<String>, entry:String, version:String):Error? {
	return withContext(Dispatchers.IO) {
		return@withContext files.toList().map { file ->
			async {
				var path = GetSandboxDBFile().path(file)
				if (file == entry) {
					path = GetSandboxDBFile().path("${version}${File.separator}${file}")
				}
				if (File(path).exists()) {
					return@async null
				}
				val ch = Channel<Error?>(1)
				val uri = URI(baseUrl).resolve(file)
				Log.d("OkDownloader", "url=${uri} path=${path}")
				val downloader = OkDownloader(uri.toString(), path, listener = object : OkDownloader.Listener {
					override suspend fun onProgress(url: String, destination: File, progress: Int) {
						Log.d(::SyncDIDBrowser.name, "download uri=${uri} path=${path} progress=${progress}")
					}

					override suspend fun onSuccess() {
						Log.d(::SyncDIDBrowser.name, "download uri=${uri} path=${path} onSuccess")
						ch.send(null)
					}

					override suspend fun onFail(err: Error) {
						Log.d(::SyncDIDBrowser.name, "download uri=${uri} path=${path} onFail err=${err}")
						ch.send(err)
					}
				})
				downloader.Start()
				return@async ch.receive()
			}
		}.awaitAll().filterNotNull().firstOrNull()
	}
}