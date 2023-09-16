package com.yhtech.did.ui.api

import com.base.foundation.sendSus
import com.base.foundation.db.DIDBlockMeta
import com.base.foundation.db.FindByRootHash
import com.base.foundation.db.insert
import com.base.foundation.getUs
import com.google.gson.Gson

class GetDIDBlockMetaRequest {
	var rootHash: Array<String> = arrayOf()
}

class GetDIDBlockMetaResponse {
	var items: Array<String> = arrayOf()
	override fun toString(): String {
		return "GetDIDBlockMetaResponse(items=${items.contentToString()})"
	}
}


suspend fun getDIDBlockMeta(rootHash:Array<String>): Error? {
	val downloaded = DIDBlockMeta.FindByRootHash(rootHash).map { it.RootHash }
	val request = GetDIDBlockMetaRequest()

    request.rootHash = 	rootHash.filter { !downloaded.contains(it) }.toTypedArray()
	if (request.rootHash.isEmpty()) {
		return null
	}

	val (ret, err) = sendSus("/im/chat/GetDIDBlockMeta2", request, GetDIDBlockMetaResponse::class.java)
	if (err != null) {
		return err
	}

	val retItems = ret.items.map { Gson().fromJson(it, DIDBlockMeta::class.java) }.toTypedArray()
	retItems.insert()

	for (retItem in retItems) {
		getUs().nc.postToMain(DIDBlockMeta.ChangedEvent(retItem.RootHash))
	}

	return null
}