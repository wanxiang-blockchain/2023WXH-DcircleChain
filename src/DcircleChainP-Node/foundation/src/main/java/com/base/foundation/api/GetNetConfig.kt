package com.base.foundation.api

import android.util.Log
import com.base.foundation.BaseUrl
import com.base.foundation.NetConfigBaseUrl
import com.base.foundation.api.http.OkhttpBuilderCreator
import com.base.foundation.db.NetConfig
import com.base.foundation.db.insert
import com.base.foundation.getUs
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class GetNetConfigRequest

class GetNetConfigResponse {
	var BaseUrl:MutableList<String> = mutableListOf()
}
suspend fun GetNetConfig():Error? {
	val net = getUs().nf.get(NetConfigBaseUrl)
	net.setBaseUrl(NetConfigBaseUrl)
	net.setHttpBuilderCreator(OkhttpBuilderCreator())

	var baseUrl = arrayOf(BaseUrl)
	val (ret, err) = postJsonNoTokenSus<GetNetConfigResponse>("/configs/config.json", GetNetConfigRequest(),
		net, GetNetConfigResponse::class.java)
	if (err==null) {
		baseUrl += ret.BaseUrl
	}

	baseUrl = baseUrl.distinct().toTypedArray()

	baseUrl.map {
		val config = NetConfig()
        config.BaseUrl = it
        config.ActiveTime = 1L
		return@map config
	}.toTypedArray().insert()

	coroutineScope {
		return@coroutineScope baseUrl.map { url ->
			async {
				Log.d(NetConfig::class.java.simpleName, "baseUrl=${url}")

				val net1 = getUs().nf.get(url)
				net1.setBaseUrl(url)
                val err1 = GetServerTime(net1)
				getUs().nf.del(net1)
				return@async err1
			}
		}.awaitAll().firstOrNull()
	}

	getUs().nf.del(net)

	return null
}