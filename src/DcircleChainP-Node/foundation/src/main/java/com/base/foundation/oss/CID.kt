package com.base.foundation.oss

import com.base.foundation.RustLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bouncycastle.jcajce.provider.digest.Keccak
import java.io.File

// Key = origin data's half hash
// 内容加密 |Version| origin data's hash |length|content| -> Aes(key, iv) -> 加密原始数据 -> encrypted data -> CID -> ObjectID={CID last 2 bytes/CID}

suspend fun getObjectHash(file:File):ByteArray = withContext(Dispatchers.Default) {
	if (!File(file.absolutePath).exists()) {
		throw Exception("GetObjectHash file $file not found")
	}
	require(file.length() > 0) { "file size must be greater than zero" }
	return@withContext RustLib.keccak256ForFile(file.absolutePath)
}
fun getObjectHash(chunk: ByteArray):ByteArray {
	val hash = Keccak.Digest256()
	hash.update(chunk)
	return hash.digest()
}
class CID(var path:String, private var hash:String) {
	override fun toString(): String {
		return "${path}/${hash}"
	}

	companion object
}

fun getObjectCID(hash:ByteArray):CID {
	try {
		val cid = RustLib.newKeccak256Cid(hash).toString(Charsets.UTF_8)
		require(cid.length>2) { "Cid size must be larger than 2 bytes" }
		return CID(cid.slice(cid.length-2 until cid.length), cid)
	} catch (e:Exception) {
		throw Error(e.localizedMessage)
	}
}

suspend fun getObjectCID(encrypted:File):CID = withContext(Dispatchers.Default){
	return@withContext getObjectCID(getObjectHash(encrypted))
}
