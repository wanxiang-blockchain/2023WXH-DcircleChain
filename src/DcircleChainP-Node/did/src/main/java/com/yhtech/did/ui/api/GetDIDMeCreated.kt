package com.yhtech.did.ui.api

import com.base.baseui.widget.getDIDLastStateDate
import com.base.foundation.sendSus
import com.base.foundation.db.DIDArticle
import com.base.foundation.db.findByAddress
import com.base.foundation.getUs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class GetDIDMeCreatedRequest

class GetDIDMeCreatedResponse {
	var address: Array<String> = arrayOf()
}

suspend fun GetDIDMeCreated(): Error? {
	val request = GetDIDMeCreatedRequest()
	val (ret, err) = sendSus("/im/chat/GetDIDMeCreated", request, GetDIDMeCreatedResponse::class.java)
	if (err != null) {
		return err
	}


	val has = DIDArticle.findByAddress(ret.address).map { it.Address }
	val address = ret.address.filter { !has.contains(it) }.toMutableList()

	if (address.isEmpty()) {
		return null
	}

	withContext(Dispatchers.IO) {
		val job1 = async {getDIDArticle(address.toTypedArray())}
		val job2 = async {
			address.map {
				async { GetDIDArticleStatWithDaily(it, arrayOf(getDIDLastStateDate())) }
			}.awaitAll()
		}
		listOf(job1, job2).awaitAll()
	}

	getUs().nc.postToMain(DIDArticle.MeCreateChangeEvent(address.toList()))
	return null
}