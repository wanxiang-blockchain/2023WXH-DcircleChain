package com.base.foundation.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

suspend fun isHostAvailable(url: String): Boolean {
	return withContext(Dispatchers.IO) {
		try {
			val timeoutDurationSeconds = 5L
			val client = OkHttpClient.Builder()
				.connectTimeout(timeoutDurationSeconds, TimeUnit.SECONDS)
				.readTimeout(timeoutDurationSeconds, TimeUnit.SECONDS)
				.writeTimeout(timeoutDurationSeconds, TimeUnit.SECONDS)
				.build()

			val request = Request.Builder()
				.url(url)
				.build()

			val response = client.newCall(request).execute()
			return@withContext response.isSuccessful
		} catch (e: Exception) {
			Log.w(::isHostAvailable.name, "isHostAvailable $url err=${e}")
			return@withContext false
		}
	}
}