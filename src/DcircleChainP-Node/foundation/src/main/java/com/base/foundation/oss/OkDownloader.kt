
import android.util.Log
import com.base.foundation.DCircleScope
import com.blankj.utilcode.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okio.*
import java.io.File
import java.io.IOException

class OkDownloader(private val url: String, private val path: String, private val listener: Listener = object : Listener {
	override suspend fun onProgress(url: String, destination: File, progress: Int) {
		Log.d("OkDownloader", "run url=${url} destination=${destination} progress=${progress}")
	}

	override suspend fun onSuccess() {
		Log.d("OkDownloader", "run url=${url} path=${path} onSuccess")
	}

	override suspend fun onFail(err: Error) {
		Log.d("OkDownloader", "run url=${url} path=${path} onFail err=${err}")
	}
}) {
	private var call:Call? = null

	interface Listener {
		suspend fun onProgress(url:String, destination:File, progress: Int)
		suspend fun onSuccess()
		suspend fun onFail(err: Error)
	}

	suspend fun Start() {
		withContext(Dispatchers.IO) {
			start(url, File(path))
		}
	}

	private fun start(url: String, destination: File) {
		destination.parent?.let {
			FileUtils.createOrExistsDir(File(it))
		}

		if (destination.exists()) {
			destination.delete()
		}

		val client = OkHttpClient.Builder()
			.addNetworkInterceptor { chain ->
				val originalResponse = chain.proceed(chain.request())
				originalResponse.newBuilder()
					.body(ProgressResponseBody(originalResponse.body!!, listener))
					.build()
			}
			.build()

		val request = Request.Builder()
			.url(url)
			.build()

		call = client.newCall(request)
		call?.enqueue(object : Callback {
			override fun onFailure(call: Call, e: IOException) {
				e.printStackTrace()
				DCircleScope.launch {
					listener.onFail(Error(e.localizedMessage))
				}
			}

			override fun onResponse(call: Call, response: Response) {
				if (!response.isSuccessful) {
					DCircleScope.launch {
						listener.onFail(Error("fail"))
					}
					return
				}

				if (response.body == null) {
					DCircleScope.launch {
						listener.onFail(Error("response is empty"))
					}
					return
				}
				response.body?.let { responseBody ->
					val bufferedSink = destination.sink().buffer()
					try {
						bufferedSink.writeAll(responseBody.source())
						DCircleScope.launch {
							listener.onSuccess()
						}
					} catch (e: IOException) {
						e.printStackTrace()
						DCircleScope.launch {
							listener.onFail(Error("response is empty"))
						}
					} finally {
						bufferedSink.close()
						responseBody.close()
					}
				}
			}
		})
	}

	inner class ProgressResponseBody(
		private val responseBody: ResponseBody,
		private val listener: Listener
	) : ResponseBody() {
		private var bufferedSource: BufferedSource? = null

		override fun contentType(): MediaType? {
			return responseBody.contentType()
		}

		override fun contentLength(): Long {
			return responseBody.contentLength()
		}

		override fun source(): BufferedSource {
			if (bufferedSource == null) {
				bufferedSource = source(responseBody.source()).buffer()
			}
			return bufferedSource!!
		}

		private fun source(source: Source): Source {
			return object : ForwardingSource(source) {
				var totalBytesRead = 0L
				var progress = 0

				@Throws(IOException::class)
				override fun read(sink: Buffer, byteCount: Long): Long {
					val bytesRead = super.read(sink, byteCount)
					totalBytesRead += if (bytesRead != -1L) bytesRead else 0L
					val progress = (100 * totalBytesRead / responseBody.contentLength()).toInt()
					if (progress>this.progress) {
						this.progress = progress
						DCircleScope.launch {
							listener.onProgress(url, destination = File(path), progress)
						}
					}

					return bytesRead
				}
			}
		}
	}
}
