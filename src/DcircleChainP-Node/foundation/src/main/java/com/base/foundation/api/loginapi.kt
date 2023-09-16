package com.base.foundation.api

import android.util.Log
import com.base.foundation.baselib.UserSpace
import com.base.foundation.db.Me
import com.base.foundation.db.setValue
import com.base.foundation.db.value
import com.base.foundation.getUs
import com.base.foundation.setUs
import com.base.foundation.utils.Tuple

class LoginRes {
	var uid: String = ""
	var token: String = ""
	var data: Result = Result()
}
class Result

class LoginRequestContent<T>(var data: T)

class LoginRequest<T>(var data: T) {
	var uri:String = ""
	var content: LoginRequestContent<T> = LoginRequestContent(this.data)
    var headers:MutableMap<String, String> = mutableMapOf()
}

suspend fun <R>PostJsonLoginWithResSus(req: LoginRequest<R>, oldUs: UserSpace, netName:String?=null) : Tuple<UserSpace, CodeError?> {
	var net = oldUs.nf.get(netName)

    // web 不需要初始化数据库，us 默认是没有初始化数据库
	if (oldUs.isValid()) {
		net.clearToken()
    }

	Log.d("PostJsonLoginWithRes", "req:${req} oldUs:${oldUs} netName ${netName}")

	val (res, err) = postJsonNoTokenSus<LoginRes>(req.uri, req.content, net, LoginRes::class.java, req.headers)
	Log.d("PostJsonLoginWithRes", "req:${req} res:${res} err ${err}")

	if (err !=null) {
		return Tuple(oldUs, err)
	}

	if (res.token.isEmpty()) {
		return Tuple(oldUs, CodeError("token is null"))
	}

	if (res.uid.isEmpty()) {
		return Tuple(oldUs, CodeError("uid is null"))
	}

	val newUs = oldUs.clone(res.uid)
	setUs(newUs)

    Me.setValue(Me.me, res.uid)
	net = newUs.nf.get(netName)
	net.setToken(res.token)
	return Tuple(newUs, null)
}

suspend fun <R>PostJsonLogin(req: LoginRequest<R>, oldUs: UserSpace, netName: String?=null):Tuple<UserSpace, CodeError?> {
	return PostJsonLoginWithResSus(req, oldUs, netName)
}

suspend fun RestoreTheLatest(): UserSpace? {
	val uid = Me.value(Me.me)
	if (uid == null || uid == Me.empty) {
		println("RestoreTheLatest can not restore the latest login account")
		return null
	}

	println("RestoreTheLatest restore the latest login account $uid")
	val newUs = getUs().clone(uid)
	setUs(newUs)

    return newUs
}