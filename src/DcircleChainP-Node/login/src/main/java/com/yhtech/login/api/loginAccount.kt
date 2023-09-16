package com.yhtech.login.api

import android.os.Build
import android.util.Log
import com.base.foundation.*
import com.base.foundation.api.AppState
import com.base.foundation.api.GetUserInfoSus
import com.base.foundation.api.setAppState
import com.base.foundation.chain.GetLoginSign
import com.base.foundation.chain.GetNonceSus
import com.base.foundation.db.*
import com.base.foundation.utils.HexUtils
import com.blankj.utilcode.util.DeviceUtils
import com.yhtech.login.getMnemonicForWallet
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import wallet.core.jni.HDWallet

suspend fun loginAccount(account: Account):Error? {
	val hdWallet = HDWallet(account.mnemonic, "")
	val (response, sErr) = ethPersonalSign(hdWallet, TPWalletSignMessage)
	if (sErr != null) {
		return sErr
	}

	if (account.guardValue.isEmpty()) {
		val neg1 = newNeg1Mnemonic(response.sign)
		val mnemonic = newMnemonic(newMnemonic())
        val byte0 = mnemonic.entropy()
		val byteNeg1 = neg1.wallet.entropy()

		val req = GetGuardValueRequest()
        req.address = response.wallet
		req.sign = neg1.sign(response.sign)

		val (res, err) = getGuardValue(req)
		if (err != null) {
			return err
		}

		if (res.result == GetGuardValueResponse.Result.InvalidSign.int) {
			return Error("invalid sign")
		}

		if (res.guardValue.isEmpty()) {
			account.guardValue = neg1.encrypt(RustLib.sssY1(byte0, byteNeg1))
			account.update(listOf(Account.guardValue))
		}

		if (res.guardValue.isNotEmpty()) {
			try {
				val decrypted = neg1.decrypt(res.guardValue)
				if (decrypted.isEmpty()) {
					return Error("decrypt guardValue is empty.")
				}
				account.guardValue = res.guardValue
                account.update(listOf(Account.guardValue))
			} catch (e: Exception) {
				return Error(e.localizedMessage)
			}
		}
	}

	val neg1 = newNeg1Mnemonic(response.sign)
	val mnemonic = newMnemonic(account.mnemonic)
    val byte0 = mnemonic.entropy()
	val byteNeg1 = neg1.wallet.entropy()
	require(byte0.size == byteNeg1.size) {
		Log.e("LoginAccount", "byte0's size (${byte0.size}) not eq byte1's size (${byteNeg1.size})")
	}

	val wallet = getMnemonicForWallet(account.mnemonic)
	val payload  = "".toByteArray()
	val (signResult, err) = GetLoginSign(wallet.privateKey, wallet.address, payload)
	if (err!=null) {
		return err
	}

	wallet.sign = signResult.getSignature()
    wallet.nonce   = signResult.getNonce()
	wallet.message = HexUtils.toHex( signResult.getMessage())
	wallet.signHash = signResult.getTxnHash()

	val request = LoginByMnemonicRequest()
	request.sign = wallet.sign.lowercase()
	request.message = wallet.message
	request.nonce   = wallet.nonce
	request.address = wallet.address
	request.signHash = wallet.signHash.lowercase()

	request.clientId = DeviceUtils.getUniqueDeviceId()
	request.clientName = Build.MODEL
	request.clientSystem = "Android ${Build.VERSION.RELEASE}"
	if (account.walletAddress.isEmpty()) {
		account.walletAddress = response.wallet
		account.update(listOf(Account.walletAddress))
	}

	request.thirdWalletRequest.address = account.walletAddress

	require(account.walletAddress.isNotEmpty()) {"account=${account.address}'s walletAddress can not be empty."}

	request.thirdWalletRequest.sign = mnemonic.sign(account.walletSign)
	request.thirdWalletRequest.guardValue = account.guardValue
	request.thirdWalletRequest.pubKey = mnemonic.publicKeyHex()
	request.thirdWalletRequest.verifyAddress = neg1.address()
    if (loginByMnemonic(request)!=null) {
		return Error("login failed")
	}

	setAppState(AppState.Foreground, onlyDB = true)
	DCircleScope.launch {
		val job1 = async {
			setAppState(AppState.Foreground)
		}

		val job3 = async {
			GetNonceSus(onlyDB = false)
		}

		val job4 = async {
			GetUserInfoSus(arrayOf(account.address))
		}

		listOf(job1, job3, job4).awaitAll()
	}

	val ppk = PPKey()
    ppk.privateKey = wallet.privateKey
    ppk.publicKey = wallet.publicKey
	ppk.address = wallet.address
	setWalletKey(ppk)

    return null
}