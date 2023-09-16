package com.base.foundation.api.http

import com.base.foundation.utils.Tuple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
class Okhttp(private val builder: HttpBuilder) : Http {
	override suspend fun send(): Tuple<String, Error?> {
		 return withContext(Dispatchers.IO) {
			val url = builder.baseUrl() + builder.uri()
			val client = OkHttpClient()
			val request = Request.Builder()
				.url(url)
				.build()
			 try {
				 val response = client.newCall(request).execute()
				 if (response.code==200) {
					 return@withContext Tuple("${response.body?.string()}", null)
				 }

				 return@withContext Tuple("", Error("failed"))
			 } catch (e:Exception) {
				 return@withContext Tuple("", Error(e.toString()))
			 }

		}
	}
}

class OkhttpBuilder(var baseUrl: String): HttpBuilder(baseUrl) {
	override fun build(): Http {
		return Okhttp(this)
	}
}

fun OkhttpBuilderCreator():(baseUrl:String) -> HttpBuilder {
	return {baseUrl: String ->  OkhttpBuilder(baseUrl)}
}