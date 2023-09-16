package com.base.foundation

import com.base.foundation.api.getAppState
import com.base.foundation.api.Net
import com.base.foundation.api.setAppState
import com.base.foundation.api.CodeError
import com.base.foundation.api.PostJsonNoTokenSusListener
import com.base.foundation.api.PostJsonSus
import com.base.foundation.db.FindOneMaybeActive
import com.base.foundation.db.NetConfig
import com.base.foundation.utils.Tuple

suspend fun <T>sendSus(uri: String, req:Any, resType: Class<T>, headers:MutableMap<String, String> = mutableMapOf()) : Tuple<T, CodeError?> {
	val net = getUs().nf.get()
    val ret = PostJsonSus(uri, req, resType, net, headers, listener = object : PostJsonNoTokenSusListener {
		override suspend fun onFindAnotherNet(net: Net): Net? {
			NetConfig.FindOneMaybeActive(NetConfig.EBusiness.AppServer)?.apply {
				net.setBaseUrl(this.BaseUrl)
				return net
			}

			return null
		}
	})

	// FIX: 主网络地址有可能会发生变化
	if (getUs().nf.get().getBaseUrl() != net.getBaseUrl()) {
		setAppState(getAppState())
	}

	return ret
}