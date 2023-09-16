package com.base.foundation.api

import android.util.Log
import com.base.foundation.db.NetConfig
import com.base.foundation.db.setActiveTime
import com.base.foundation.db.Token
import java.util.*

class GetServerTimeRequest

class GetServerTimeResponse {
	var serverTime:Long = 0
}

suspend fun GetServerTime(net:Net, retry:Int = 3):Error? {
	val cfg = NetConfig()
    cfg.BaseUrl = net.getBaseUrl()
    val start = Date().time

	val (_, err) = postJsonNoTokenSus<Response<GetServerTimeResponse>>("/im/user/getServerTime", Request(Token.empty, GetServerTimeRequest()),
		net, Response(0, GetServerTimeResponse::class.java)::class.java)
	if (err!=null && retry > 0) {
		return GetServerTime(net, retry -1)
	}

	if (err==null) {
		cfg.ActiveTime = Date().time
		cfg.TTL = (cfg.ActiveTime - start).toInt()
		cfg.setActiveTime()
        return null
    }

	Log.d(NetConfig::class.java.simpleName, "GetServerTime err=${err}")
	if (err.toString().contains("java.net.SocketTimeoutException")) {
		cfg.ActiveTime = 0
		cfg.setActiveTime()
        return err
	}

	cfg.ActiveTime = 1
    cfg.setActiveTime()

	return null
}