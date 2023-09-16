package com.yhtech.did.ui.api

import com.base.foundation.sendSus
import com.base.foundation.db.*
import com.base.foundation.getUs

class GetDIDMeBuyRequest {
	var address: Array<String> = arrayOf()
}

class GetDIDMeBuyResponseItem {
	var address:String = ""
	var secretKey:String = ""
	var payStatus:Int = 0
    var createTime:Long = 0
}
class GetDIDMeBuyResponse {
	var items: Array<GetDIDMeBuyResponseItem> = arrayOf()
	override fun toString(): String {
		return "GetDIDMeBuyResponse(items=${items.contentToString()})"
	}
}


suspend fun GetDIDMeBuy(address:Array<String> = arrayOf()): Error? {
	val request = GetDIDMeBuyRequest()
    request.address = address

	val (ret, err) = sendSus("/im/chat/GetDIDMeBuy", request, GetDIDMeBuyResponse::class.java)
	if (err != null) {
		return err
	}

	ret.items.map { item ->
		val article = DIDArticle()
        article.Address = item.address
		article.PayStatus = item.payStatus
		article.SecretKey = item.secretKey
		article.PayTime = item.createTime
		return@map article
	}.toTypedArray().update(mutableListOf(DIDArticle.PayStatus, DIDArticle.SecretKey, DIDArticle.PayTime))

	getDIDArticle(ret.items.map { it.address }.toTypedArray())?.apply {
		return this
	}

	getUs().nc.postToMain(DIDArticle.MeCreateChangeEvent(ret.items.map { it.address }.toList()))
	return null
}