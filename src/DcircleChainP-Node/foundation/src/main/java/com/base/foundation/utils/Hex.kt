package com.base.foundation.utils

import android.util.Log


fun fromHex(hex:String) : ByteArray {
	val len = hex.length
	if ((len%2)>0) {
		throw Error("hex(${hex}'s length invalid)")
	}

	val result = ByteArray(len / 2)

	for (i in 0 until len step 2) {
		try {
			val digit = Integer.parseInt(hex.substring(i, i + 2), 16)
			result[i / 2] = digit.toByte()
		} catch (e:Exception) {
			Log.e("fromHex", "error $e")
		}
	}

	return result
}

fun toHex(bytes:ByteArray):String {
	return bytes.joinToString("") { "%02x".format(it) }
}