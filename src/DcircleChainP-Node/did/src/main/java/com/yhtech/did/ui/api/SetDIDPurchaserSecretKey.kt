package com.yhtech.did.ui.api

import com.base.foundation.Aes
import com.base.foundation.EccLoadUtils
import com.base.foundation.sendSus
import com.base.foundation.api.ReqId
import com.base.foundation.db.DIDArticle
import com.base.foundation.db.findByAddress
import com.base.foundation.getWalletKey
import com.base.foundation.utils.fromHex
import com.google.gson.Gson

private class SetDIDPurchaserSecretKeyRequest {
    var didAddress:String = ""
    var secretKey: String = ""
    var uid: String = ""
}

private class SetDIDPurchaserSecretKeyResponse

suspend fun setDIDPurchaserSecretKey(address:String, uid: String):Error?{
    if (address.isEmpty() || uid.isEmpty()){
        return null
    }
    val aes =  getDidSecretKey(address)?:return Error("not found")

    if (aes.key.isEmpty()){
        return null
    }
    val (secretKey, _, err1) = newSecretKeySus(uid, aes)
    if (err1 !=null) {
        return err1
    }

    val req = SetDIDPurchaserSecretKeyRequest()
    req.didAddress = address
    req.uid = uid
    req.secretKey = secretKey
    val (ret, err2) = sendSus("/im/chat/SetDIDPurchaserSecretKey", req, SetDIDPurchaserSecretKeyResponse::class.java, mutableMapOf(
        Pair(ReqId, "${address}-${uid}")
    ))
    if (err2 != null) {
        return err2
    }
    return null
}

suspend fun getDidSecretKey(didAddress: String): Aes? {
    val doc  = DIDArticle.findByAddress(didAddress) ?: return null
    if (doc.SecretKey.isEmpty()) {
        return null
    }

    return try {
        val decrypted = EccLoadUtils().eccDecrypt(getWalletKey().privateKey, doc.SecretKey)//getPrikey
        Gson().fromJson(String(fromHex(decrypted)), Aes::class.java)
    } catch (e: Exception) {
        null
    }
}