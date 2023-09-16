package com.yhtech.login.api

import com.base.foundation.EccLoadUtils
import com.base.foundation.utils.Tuple
import com.base.foundation.utils.fromHex
import com.base.foundation.utils.toHex
import com.base.thridpart.sha256
import com.base.thridpart.toHexString
import com.blankj.utilcode.util.LogUtils
import wallet.core.jni.*


class AskTPWalletAuthorizeLoginResponse {
	var sign: String = ""
	var version: String = ""
	var wallet: String = ""
}



fun ethPersonalSign(wallet: HDWallet, message:String):Tuple<AskTPWalletAuthorizeLoginResponse,Error?> {
	val data="\u0019Ethereum Signed Message:\n${message.length}${message}"
    LogUtils.e("GetGuardValue",data)
	val key = wallet.getKeyForCoin(CoinType.ETHEREUM)
	val rlpHash = Hash.keccak256(data.toByteArray())
	val signBytes = key.sign(rlpHash, Curve.SECP256K1)

	signBytes[signBytes.size-1] = (signBytes[signBytes.size-1] + 27).toByte()
	val signature = signBytes.toHexString()

	val response = AskTPWalletAuthorizeLoginResponse()
    response.sign = "0x$signature"
	response.wallet = wallet.getAddressForCoin(CoinType.ETHEREUM)
	LogUtils.e("GetGuardValue wallet",response.wallet)
	return Tuple(response, null)
}
class Mnemonic(var name:String, var wallet: HDWallet) {
	private var privateKey: PrivateKey = wallet.getKeyForCoin(CoinType.ETHEREUM)

    fun publicKeyHex():String {
		return privateKey.getPublicKeySecp256k1(true).data().toHexString()
	}

	private fun privateKeyHex():String {
		return privateKey.data().toHexString()
	}

	fun encrypt(data: ByteArray): String {
		return EccLoadUtils().eccEncrypt(publicKeyHex(), toHex(data))
	}

	fun decrypt(encrypted: String): ByteArray {
		return fromHex(EccLoadUtils().eccDecrypt(privateKeyHex(), encrypted))
	}

	fun sign(data:String):String {
		val hash = data.sha256()
		return privateKey.signAsDER(hash).toHexString()
	}

	fun mnemonic():String {
		return wallet.mnemonic()
    }

	fun entropy():ByteArray {
		return wallet.entropy()
	}

	fun address():String {
		return wallet.getAddressForCoin(CoinType.ETHEREUM)
	}
}

fun newNeg1Mnemonic(signature: String): Mnemonic {
	val mnemonic = (signature + "Neg1").sha256()
    val wallet = HDWallet(mnemonic.slice(0 until 16).toByteArray(), "")
	return Mnemonic("Neg1", wallet)
}

fun newMnemonic(mnemonic:String): Mnemonic {
	val wallet = HDWallet(mnemonic, "")
	return Mnemonic("SecretChatSign", wallet)
}

fun newMnemonic() : String {
	return HDWallet(128,"").mnemonic()
}