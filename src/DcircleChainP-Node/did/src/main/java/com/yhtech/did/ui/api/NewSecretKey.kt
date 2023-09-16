package com.yhtech.did.ui.api

import com.base.foundation.Aes
import com.base.foundation.EccLoadUtils
import com.base.thridpart.toJson
import com.base.foundation.utils.HexUtils
import com.blankj.utilcode.util.LogUtils

suspend fun newSecretKeySus(uid: String, aes: Aes):Triple<String, String, Error?> {
	val it = findPubKeySus(uid)
	if (it.err != null) {
		return Triple("", "", it.err)
	}
	val enc = HexUtils.toHex(aes.toJson().toByteArray())
	LogUtils.d("it.value:\n${it.value},ecc-aes:\n${enc}")
	val eccData = EccLoadUtils().eccEncrypt(it.value, enc)
	return Triple(eccData, uid, null)
}


