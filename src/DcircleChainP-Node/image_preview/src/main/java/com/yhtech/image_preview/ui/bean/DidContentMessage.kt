package com.yhtech.image_preview.ui.bean

import android.util.Log
import com.base.foundation.Aes
import com.base.foundation.EccLoadUtils
import com.base.foundation.getWalletKey
import com.base.foundation.utils.fromHex
import com.google.gson.Gson


fun GetDIDAesKey(secretKey:String):Aes?{
  if (secretKey.isEmpty()) {
    Log.w("GetDIDAesKey", "secretKey is empty")
    return null
  }

  if (getWalletKey().privateKey.isEmpty()) {
    Log.w("GetDIDAesKey", "privateKey is empty")
    return null
  }

  Log.d("GetDIDAesKey", "secretKey=${secretKey}")
  return try {
    val decrypted = EccLoadUtils().eccDecrypt(getWalletKey().privateKey,secretKey)
    Gson().fromJson(String(fromHex(decrypted)), Aes::class.java)
  } catch (e: java.lang.Exception) {
    Log.w("GetDIDAesKey", "secretKey=${secretKey} err=${e}")
    null
  }
}

