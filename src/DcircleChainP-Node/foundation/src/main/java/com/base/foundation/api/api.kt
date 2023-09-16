package com.base.foundation.api

import android.util.Log
import com.base.foundation.db.NetConfig
import com.base.foundation.db.setActiveTime
import com.base.foundation.db.Token
import com.base.foundation.db.insert
import com.base.foundation.utils.Promise
import com.base.foundation.utils.Tuple
import com.base.foundation.utils.toPromise
import com.google.gson.Gson
import java.lang.reflect.Type
import java.util.*
import kotlin.random.Random


class Code {
	companion object {
		var TokenExpireCode = 401
		var Unknown = 500
		var Success = 200
	}
}

class CodeError(var err: String = "", var code: Int = Code.Unknown) : Error() {
	override fun toString(): String {
		return "err:${err}, code:${code}"
	}
}

const val ReqId = "X-Req-Id"
var api = "api"

class Request<T>(var token: String, var data: T)

class Response<T>(var code: Int, var data: T)

suspend fun <T> PostJsonSus(uri: String, req: Any, resType: Class<T>, net: Net, headers: MutableMap<String, String> = mutableMapOf(), listener: PostJsonNoTokenSusListener = object :
	PostJsonNoTokenSusListener {
	override suspend fun onFindAnotherNet(net: Net): Net? {
		return null
	}
}): Tuple<T, CodeError?> {
	if (net.is401()) {
		return Tuple(null as T, CodeError("has 401", Code.TokenExpireCode))
	}

	val token = net.getToken()
	// token为空，需要处理401，并且不执行真正的网络请求。
	if (token == Token.empty) {
		net.process401()
        return Tuple(null as T, CodeError("token is empty", Code.TokenExpireCode))
	}

	val r = Request(token, req)

	val (value, err) = postJsonNoTokenSus<Response<T>>(uri, r, net, Response(0, resType::class.java)::class.java, headers, listener)

	if (err != null) {
		return Tuple(null as T, err)
	}

	if (value.code != Code.TokenExpireCode) {
		// 取消401
		net.clear401()
		// 直接返回，类似 GetUserInfo 接口会提示 com.google.gson.internal.LinkedTreeMap cannot be cast to GetUserInfoResponse
		// return@Task Promise.resolve(Tuple(value.data, null))
		val data = Gson().fromJson(Gson().toJson(value.data), resType)
		return Tuple(data, null)
	}

	if (token !== net.getToken()) {
		return Tuple(null as T, CodeError("token is too old", Code.TokenExpireCode))
	}

	net.process401()
    return Tuple(null as T, CodeError("token is expire", Code.TokenExpireCode))
}

fun SignNonceStr(len: Int = 20): String {
	val letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
	val randomString = StringBuilder()
	repeat(len) {
		val index = Random.nextInt(letters.length)
		val letter = letters[index]
		randomString.append(letter)
	}
	return randomString.toString()
}

interface PostJsonNoTokenSusListener {
	suspend fun onFindAnotherNet(net: Net): Net?
}

suspend fun <T> postJsonNoTokenSus(uri: String, req: Any, net: Net, resType: Type, headers: MutableMap<String, String> = mutableMapOf(), listener: PostJsonNoTokenSusListener = object :
	PostJsonNoTokenSusListener {
	override suspend fun onFindAnotherNet(net: Net): Net? {
		return null
	}
}): Tuple<T, CodeError?> {
	headers[api] = uri
    if (headers[ReqId]==null) {
		headers[ReqId] = SignNonceStr()
	}
	val reqid = headers[ReqId]

	Log.d("PostJsonNoToken", "net:${net.getBaseUrl()} uri:${uri} reqid:${reqid} to ${net.getBaseUrl()}")

	val reqStr = Gson().toJson(req)

	val start = Date().time
	Log.d("PostJsonNoToken", "net:${net.getBaseUrl()} uri:${uri} reqid:${reqid} request: ${reqStr} start=${start}")

	val (response, err) = net.getHttpBuilder().setUri(uri).setHeaders(headers)
		.setContent(reqStr).build().send()
	if (err != null) {
		Log.w("PostJsonNoTokenThen", "net:${net.getBaseUrl()} uri:${uri} reqid:$reqid response err: $err")

		// 1、此两种错误可以明确知道是网络链接异常了
		if (err.toString().contains("java.net.SocketTimeoutException") || err.toString().contains("javax.net.ssl.SSLHandshakeException")) {
			val cfg = NetConfig()
            cfg.BaseUrl = net.getBaseUrl()
            cfg.ActiveTime = 0
            if (cfg.insert()!=null) {
				cfg.setActiveTime()
			}

			listener.onFindAnotherNet(net)?.apply {
				return postJsonNoTokenSus(uri, req, this, resType, headers)
			}
		}

		// 2、除了逻辑服务器错误了，有可能网络链接异常了
		if (!(err.toString().contains("Internal Server Error") || err.toString().contains("Not Found"))) {
			val cfg = NetConfig()
            cfg.BaseUrl = net.getBaseUrl()
            cfg.ActiveTime = 1
            if (cfg.insert()!=null) {
				cfg.setActiveTime()
			}

			listener.onFindAnotherNet(net)?.apply {
				return postJsonNoTokenSus(uri, req, this, resType, headers)
			}
		}

		return Tuple(null as T, CodeError(err.toString()))
	}

	Log.d("PostJsonNoTokenThen", "net:${net.getBaseUrl()} uri:${uri} reqid:$reqid response: $response end=${Date().time - start}")
	if (response == "") {
		return Tuple(null as T, CodeError("response is empty"))
	}
	return try {
		Tuple(Gson().fromJson(response, resType), null)
	} catch (e: Exception) {
		Tuple(null as T, CodeError(e.toString()))
	}
}

@Deprecated("使用PostJsonNoTokenSus")
fun <T> PostJsonNoToken(uri: String, req: Any, net: Net, resType: Type, headers: MutableMap<String, String> = mutableMapOf()): Promise<Tuple<T, CodeError?>> {
	return toPromise { postJsonNoTokenSus(uri, req, net, resType, headers) }
}