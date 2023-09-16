package com.base.foundation.ts

import com.base.foundation.api.Net
import com.base.foundation.api.CodeError

val waitingRegistry:MutableMap<String, MutableList<suspend (err: CodeError?)->Unit>> = mutableMapOf()

suspend fun RegisterAutoLogin(net:Net, login:suspend () -> CodeError?) {
	waitingRegistry[net.getBaseUrl()] = mutableListOf()
	net.set401Delegate {
		val err = login()
		val waiting = waitingRegistry[net.getBaseUrl()]
		waiting?.forEach { w ->
			w(err)
		}

		waitingRegistry[net.getBaseUrl()] = mutableListOf()

		return@set401Delegate null
	}
}