package com.base.foundation.api.http

import com.anywithyou.stream.Client.PushCallback
import com.base.foundation.utils.Tuple


interface Http {
	suspend fun send(): Tuple<String, Error?>
}

abstract class HttpBuilder(url: String) {
	abstract fun build(): Http

	private var pusher_: PushCallback?=null
	private var baseUrl_ = url
	private var uri_ = ""
	private var headers_:MutableMap<String, String> = mutableMapOf()
	private var content_:String = ""


	fun setContent(content:String): HttpBuilder {
		content_ = content
		return this
	}

	fun content(): String {
		return this.content_
	}

	fun baseUrl():String {
		return baseUrl_
	}

	fun setUri(uri:String): HttpBuilder {
		uri_ = uri
		return this
	}

	fun uri():String {
		return uri_
	}

	fun setHeaders(headers:MutableMap<String, String>): HttpBuilder {
		headers_ = headers
		return this
	}

	fun pusher(pushCallback: PushCallback): HttpBuilder {
		this.pusher_ = pushCallback
		return this
	}

	fun addHeader(key:String, value:String): HttpBuilder {
		headers_[key] = value
		return this
	}

	fun headers():Map<String, String> {
		return headers_
	}
}