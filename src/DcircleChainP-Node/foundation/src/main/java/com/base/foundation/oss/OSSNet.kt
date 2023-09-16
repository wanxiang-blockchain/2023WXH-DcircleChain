package com.base.foundation.oss

import android.util.Log
import com.base.foundation.Aes
import com.base.foundation.DecryptFileInvalidException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

enum class Priority(var value: Int) {
	Channel1(0x01),
	Channel4(0x04);

	companion object
}

fun Priority.Companion.valueOf(value: Int):Priority? {
	for (color in Priority.values()) {
		if (color.value == value) {
			return color
		}
	}
	return null
}

suspend fun DecryptOSSToDBFile(objectId: String, aes: Aes): Error? = withContext(Dispatchers.Default) {
	Log.d("OSSEncryptor", "${::DecryptOSSToDBFile.name} cid=${objectId} aes=${aes.key} start")
	val to = GetSandboxDBFile().path(objectId)
	if (File(to).exists()) {
		return@withContext null
	}

	val tmp = GetSandboxDBFile().path("$objectId.tmp.${::DecryptOSSToDBFile.name}")
	if (File(tmp).exists()) {
		if (!File(tmp).delete()) {
			return@withContext Error("delete tmp failed")
		}
	}

	val from = GetSandboxOSSFile().path(objectId)
	if (!File(from).exists()) {
		return@withContext Error("objectId(${objectId}) not found")
	}

	try {
		aes.decryptWithFilePath(from, tmp)
		Log.d("OSSEncryptor", "${::DecryptOSSToDBFile.name} cid=${objectId} aes=${aes.key} end")
	} catch (e: DecryptFileInvalidException) {
		GetSandboxOSSFile().delete(objectId)
		return@withContext Error(e.toString())
	} catch (e: java.lang.Exception) {
		Log.e("OSSEncryptor", "${::EncryptFileToOSSFile.name} decryptWithFilePath err=${e}")
		return@withContext Error(e.toString())
	}

	return@withContext GetSandboxDBFile().mv(File(tmp), File(to))
}

suspend fun EncryptFileToOSSFile(from: File, aes: Aes): Pair<CID?, Error?> = withContext(Dispatchers.Default) {
	Log.d("OSSEncryptor", "${::EncryptFileToOSSFile.name} file=${from} aes=${aes.key}")
	val objectId = from.name
	val tmp = GetSandboxOSSFile().path("$objectId.tmp.${::EncryptFileToOSSFile.name}")
	if (File(tmp).exists()) {
		if (!File(tmp).delete()) {
			return@withContext Pair(null, Error("delete tmp failed"))
		}
	}
	val to = GetSandboxOSSFile().path(objectId)

	try {
		aes.encryptWithFilePath(from.absolutePath, tmp)
	} catch (e: java.lang.Exception) {
		Log.e("OSSEncryptor", "${::EncryptFileToOSSFile.name} encryptWithFilePath err=${e}")
		return@withContext Pair(null, Error(e.localizedMessage))
	}

	val err = GetSandboxOSSFile().mv(File(tmp), File(to))
	if (err != null) {
		return@withContext Pair(null, err)
    }

	val cid = getObjectCID(File(to))
	Log.d("OSSEncryptor", "${::EncryptFileToOSSFile.name} cid=${cid} aes=${aes.key}")
	return@withContext Pair(cid, GetSandboxOSSFile().mv(File(to), File(GetSandboxOSSFile().path(cid.toString()))))
}

