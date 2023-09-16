package com.base.foundation.api

import com.base.foundation.NetConfigBaseUrl
import com.base.foundation.api.http.OkhttpBuilderCreator
import com.base.foundation.db.*
import com.base.foundation.getUs
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.util.*

class GetShareConfigRequest

class GetShareConfigResponse {
	var BaseUrl:MutableList<String> = mutableListOf()
}

suspend fun GetShareConfig():Error? {
	val net = getUs().nf.get(NetConfigBaseUrl)
	net.setBaseUrl(NetConfigBaseUrl)
	net.setHttpBuilderCreator(OkhttpBuilderCreator())

	val (ret, err) = postJsonNoTokenSus<GetShareConfigResponse>("/configs/shareconfig.json", GetShareConfigRequest(),
		net, GetShareConfigResponse::class.java)
	if (err!=null) {
		delay(5000)
		return GetShareConfig()
	}

	val docs = NetConfig.FindAllDocs()
	for (doc in docs) {
		if (!ret.BaseUrl.contains(doc.BaseUrl)) {
			doc.delete()
		}
	}

	ret.BaseUrl.map {
		val config = NetConfig()
        config.BaseUrl = it
        config.ActiveTime = 1L
		config.Business = NetConfig.EBusiness.ShareLink.int
		return@map config
	}.toTypedArray().insert()

	coroutineScope {
		return@coroutineScope ret.BaseUrl.map { url ->
			async {
				val start = Date().time
				val available = isHostAvailable(url)
				val config = NetConfig()
				config.Business = NetConfig.EBusiness.ShareLink.int
				config.BaseUrl = url
				config.ActiveTime = Date().time
				config.TTL = (config.ActiveTime - start).toInt()
				if (!available) {
					config.ActiveTime = 0
					config.TTL = 0
				}
				config.setActiveTime()
				return@async
			}
		}.awaitAll().firstOrNull()
	}

	getUs().nf.del(net)

	return null
}