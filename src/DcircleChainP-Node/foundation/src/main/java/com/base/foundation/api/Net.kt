package com.base.foundation.api

import com.base.foundation.api.http.HttpBuilder
import com.base.foundation.api.http.Stream
import com.base.foundation.api.http.StreamBuilderCreator
import com.base.foundation.db.Token
import com.base.foundation.db.clear
import com.base.foundation.db.getValue
import com.base.foundation.db.setValue

open class NetFactory {
	fun get(name:String, token: Token): Net {
		var old = this.nets[name]
		if (old === null) {
			old =  Net(name, token)
			this.nets[name] = old
		}

		return old
	}

	fun del(net:Net) {
		this.nets.remove(net.name)
		Stream.allClients.remove(net.getBaseUrl())
	}

	private var nets:MutableMap<String, Net> = mutableMapOf()
}


open class Net(var name:String, var token: Token) {
	suspend fun process401() {
		if (has401_) {
			return
		}

		has401_ = true
		net401Delegate_(this)
	}

	fun set401Delegate(net401Delegate:suspend (net: Net)->Unit?) {
		net401Delegate_ = net401Delegate
	}

	fun is401(): Boolean {
		return has401_
	}

	fun clear401() {
		has401_ = false
	}

	fun setBaseUrl(url:String) {
		baseUrl_ = url
	}

	fun getBaseUrl():String {
		return baseUrl_
	}

	fun setHttpBuilderCreator(creator:(baseUrl:String) -> HttpBuilder) {
		this.creator_ = creator
	}

	fun getHttpBuilder(): HttpBuilder {
		return this.creator_(this.getBaseUrl())
	}

	suspend fun setToken(token:String) {
		if (token !== Token.empty) {
			this.clear401()
		}

		token_ = token
		this.token.token = token
        this.token.setValue()
        if (this.name != this.token.id) {
			throw Error("setToken this.name(${this.name}) not eq this.token.id(${this.token.id})")
		}

		if (Token.getValue(this.name) != token) {
			throw Error("get (${Token.getValue(this.name)}) and set (${token}) token not eq")
		}
	}

	suspend fun getToken():String {
		if (this.token_ == Token.empty) {
			this.token_ = Token.getValue(this.name)
		}
		return token_
	}

	suspend fun clearToken() {
		this.token_ = ""
		this.token.clear()
	}

	private var token_:String = Token.empty
	private var has401_:Boolean = false
	private var baseUrl_:String = ""
	private var net401Delegate_:suspend (net: Net)->Unit? = {}
	private var creator_:(baseUrl:String)-> HttpBuilder = StreamBuilderCreator()
}