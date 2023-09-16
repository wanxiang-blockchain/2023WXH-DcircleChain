package com.base.foundation.api

import com.base.foundation.sendSus
import com.base.foundation.db.InsertOrUpdate
import com.base.foundation.db.User
import com.base.foundation.getUs

class GetUserInfoRequest {
    var uidS: Array<String> = arrayOf()
}


class GetUserInfoResponse {
    var items: Array<GetUserInfoResponseItem> = arrayOf()
    override fun toString(): String {
        return "GetUserInfoResponse(items=${items.contentToString()})"
    }
}

class GetUserInfoResponseItem {
    val uid: String = ""
    val pubKey: String = ""
    val nameEn: String = ""
    val name: String = ""
    val inviteCode: String = ""
}


suspend fun GetUserInfoSus(uidS: Array<String>): Error? {
	val request = GetUserInfoRequest()
    request.uidS = uidS
	if (request.uidS.isEmpty()) {
		return null
	}

	val (ret, err) = sendSus("/im/chat/GetUserInfo", request, GetUserInfoResponse::class.java)
	if (err != null) {
		return err
	}

	if (ret.items.isEmpty()) {
		return null
	}

	val retItems = ret.items.map {
		val user = User()
        user.Name = it.name
		user.NameEn = it.nameEn
		user.Uid = it.uid
		user.Pubkey = it.pubKey
		user.InviteCode = it.inviteCode
		return@map user
	}

	val failed = retItems.toTypedArray().InsertOrUpdate()
	val oks = ret.items.filter { !failed.contains( it.uid) }.map { it.uid }

	if (oks.isNotEmpty()) {
		getUs().nc.postToMain(User.ChangedEvent(oks))
	}

	for (uid in oks) {
		getUs().nc.postToMain(User.ChangedEvent(uid))
	}

	return null
}