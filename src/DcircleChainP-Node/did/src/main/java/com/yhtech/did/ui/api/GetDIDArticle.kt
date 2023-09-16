package com.yhtech.did.ui.api

import com.base.foundation.sendSus
import com.base.foundation.db.*
import com.base.foundation.getUs

class GetDIDArticleRequest {
	var address: Array<String> = arrayOf()
}

class GetDIDArticleResponse {
	var items: Array<DIDArticle> = arrayOf()
	override fun toString(): String {
		return "GetDIDArticleResponse(items=${items.contentToString()})"
	}
}

suspend fun getDIDArticle(address: Array<String>): Error? {
	if (address.isEmpty()) {
		return null
	}

	val request = GetDIDArticleRequest()
    request.address = address

	val (ret, err) = sendSus("/im/chat/GetDIDArticle", request, GetDIDArticleResponse::class.java)
	if (err != null) {
		return err
	}

	if (ret.items.isEmpty()) {
		return null
	}

	val blockRootHash:MutableList<String> = mutableListOf()
    blockRootHash.addAll(ret.items.map { it.CurrentBlockRootHash }.filter { it.isNotEmpty() })
	blockRootHash.addAll(ret.items.map { it.EditingBlockRootHash }.filter { it.isNotEmpty() })
	val (blocks, _) = getDIDArticleBlock(blockRootHash.filter { it.isNotEmpty() }
        .toTypedArray())

	val metaRootHash:MutableList<String> = mutableListOf()
	for (block in blocks) {
		metaRootHash.add(block.ARMetaRootHash)
		metaRootHash.add(block.EncRootHash)
		metaRootHash.add(block.FeatureRootHash)
	}
    getDIDBlockMeta(metaRootHash.filter { it.isNotEmpty() }.toTypedArray())

	val retItems = ret.items
	val failed = retItems.insert()
	if (failed.isNotEmpty()) {
		val updates = (retItems.filter { item -> failed.contains(item.Address) }).toTypedArray()
		val columns = mutableListOf(DIDArticle.CurrentBlockRootHash, DIDArticle.visible, DIDArticle.usedVersion, DIDArticle.UpdateTime, DIDArticle.editingDevice)
		updates.update(columns)

		val updates2 = (retItems.filter { item -> failed.contains(item.Address) }).filter {
			return@filter true
		}.toTypedArray()
		updates2.update(mutableListOf(DIDArticle.Status))
	}

	val event = DIDArticle.ChangedEvent()
    event.ids = retItems.map { it.Address }
	getUs().nc.postToMain(event)
	for (retItem in retItems) {
		getUs().nc.postToMain(DIDArticle.ChangedEvent(retItem.Address))
	}
	return null
}