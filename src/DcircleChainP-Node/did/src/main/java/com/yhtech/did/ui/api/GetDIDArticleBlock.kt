package com.yhtech.did.ui.api

import com.base.foundation.sendSus
import com.base.foundation.db.DIDArticleBlock
import com.base.foundation.db.FindByRootHash
import com.base.foundation.db.insert
import com.base.foundation.getUs

class GetDIDArticleBlockRequest {
	var rootHash: Array<String> = arrayOf()
}

class GetDIDArticleBlockResponse {
	var items: Array<DIDArticleBlock> = arrayOf()
	override fun toString(): String {
		return "GetDIDArticleBlockResponse(items=${items.contentToString()})"
	}
}


suspend fun getDIDArticleBlock(rootHash:Array<String>): Pair<Array<DIDArticleBlock>, Error?> {
	val downloaded = DIDArticleBlock.FindByRootHash(rootHash).map { it.RootHash }
	val request = GetDIDArticleBlockRequest()
    request.rootHash = 	rootHash.filter { !downloaded.contains(it) }.toTypedArray()
	if (request.rootHash.isEmpty()) {
		return Pair(DIDArticleBlock.FindByRootHash(rootHash), null)
	}

	val (ret, err) = sendSus("/im/chat/GetDIDArticleBlock", request, GetDIDArticleBlockResponse::class.java)
	if (err != null) {
		return Pair(DIDArticleBlock.FindByRootHash(rootHash), err)
	}

	ret.items.insert()
	for (item in ret.items) {
		getUs().nc.postToMain(DIDArticleBlock.ChangedEvent(item.RootHash))
	}

	return Pair(DIDArticleBlock.FindByRootHash(rootHash), null)
}