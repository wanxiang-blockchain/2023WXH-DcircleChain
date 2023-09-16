package com.yhtech.did.ui.api

import com.base.foundation.api.GetUserInfoSus
import com.base.foundation.db.FindById
import com.base.foundation.db.User
import com.base.foundation.utils.Tuple

suspend fun findPubKeySus(uid: String, retry: Int = 3): Tuple<String, Error?> {
	if (retry <= 0) {
		return Tuple("", Error("FindPubKey retry out"))
	}
	val doc = User.FindById(uid)
	if (doc!=null && doc.Uid.isNotEmpty() && doc.Pubkey.isNotEmpty()) {
		return Tuple(doc.Pubkey, null)
	}
	val err = GetUserInfoSus(arrayOf(uid))
	if (err!=null) {
		return Tuple("", err)
	}
	return findPubKeySus(uid, retry - 1)
}