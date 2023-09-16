package com.yhtech.login.api

import android.util.Log
import com.anywithyou.stream.Duration
import com.base.baseui.widget.ui.AskDCircleSignatureRequest
import com.base.baseui.widget.ui.AskDCircleSignatureResponse
import com.base.foundation.AppChannel
import com.base.foundation.BranchName
import com.base.foundation.DCircleScope
import com.base.foundation.RustLib
import com.base.foundation.TPWalletSignMessage
import com.base.foundation.VersionCode
import com.base.foundation.api.LoginRequest
import com.base.foundation.api.PostJsonLogin
import com.base.foundation.chain.DCircle_ADDRESS
import com.base.foundation.chain.GetNonceSus
import com.base.foundation.chain.GetSignResult
import com.base.foundation.chain.GetSignResultItem
import com.base.foundation.chain.OpCode
import com.base.foundation.db.Account
import com.base.foundation.db.AllColumns
import com.base.foundation.db.find
import com.base.foundation.db.findByAddress
import com.base.foundation.db.getPassword
import com.base.foundation.db.Me
import com.base.foundation.db.createTime
import com.base.foundation.db.insert
import com.base.foundation.db.setValue
import com.base.foundation.db.update
import com.base.foundation.demoMnemonic
import com.base.foundation.demoPassword
import com.base.foundation.getUs
import com.base.foundation.setUs
import com.base.foundation.utils.toHex
import com.base.foundation.utils.HexUtils
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import wallet.core.jni.HDWallet
import wallet.core.jni.Mnemonic
import java.math.BigInteger
import java.util.Date

class LoginByThirdWalletRequest {
	var address: String = ""
	var guardValue: String = ""
	var pubKey: String = ""
	var sign: String = ""
	var verifyAddress: String = ""
}
class LoginByMnemonicRequest {
	var sign:String = ""
	var address:String = ""
	var signHash: String = ""
	var nonce:BigInteger = BigInteger.valueOf(0)
	var message:String = ""
	var clientId:String = ""
	var clientName:String = ""
	var clientSystem:String = ""
	var versionCode:Int = VersionCode
	var appVersion:String = BranchName
	var appChannel:String = AppChannel
	val thirdWalletRequest:LoginByThirdWalletRequest = LoginByThirdWalletRequest()
}

suspend fun loginByMnemonic(request:LoginByMnemonicRequest) : Error? {
	val req = LoginRequest(request)
    req.uri = "/im/user/loginByMnemonic"

	val (us, err) = PostJsonLogin(req, getUs())
	if (err!=null) {
		return err
	}

	setUs(us)
	us.nf.get().set401Delegate { net ->
		Log.w("401", "net=${net.name} baseUrl=${net.getBaseUrl()} has 401")
		DCircleScope.launch {
			Me.setValue(Me.me, Me.empty)
			val docs = Account.find(1)
			if (docs.isEmpty()) {
				net.clear401()
				login(demoMnemonic)
				return@launch
			}
			net.clear401()
			login(demoMnemonic)
		}
		return@set401Delegate null
	}

	val account = Account()
	account.address = request.address
	account.createTime = Date().time
	if (account.update(listOf(Account.createTime))!=null) {
		Account.ChangedEvent().apply {
			this.ids = listOf(request.address)
			getUs().nc.postToMain(this)
		}

	}
	return null
}

private suspend fun login(words: String) {
	val checkM = Mnemonic.isValid(words)
	if (!checkM){
		return
	}
	val mnemonic = newMnemonic(words)
	val doc = Account()
	doc.from = Account.From.ImportMnemonic.int
	doc.mnemonic = mnemonic.mnemonic()
	doc.address = mnemonic.address()

	create(doc)
}

private suspend fun create(doc: Account) {
//        binding.tvConfirm.showLoading(true)
	do {
		val wallet = HDWallet(doc.mnemonic, "")
		val aes = Account.getPassword(demoPassword)
		val mnemonic = newMnemonic(doc.mnemonic)
		try {
			doc.mnemonic = toHex(aes.encrypt(doc.mnemonic.toByteArray(Charsets.UTF_8)))
		} catch (e: Exception) {
			break
		}

		if (doc.guardValue.isEmpty()) {
			val (response, err0) = ethPersonalSign(wallet, TPWalletSignMessage)
			if (err0 != null) {
				break
			}
			val neg1 = newNeg1Mnemonic(response.sign)
			val byte0 = mnemonic.entropy()
			val byteNeg1 = neg1.wallet.entropy()

			val req = GetGuardValueRequest()
            req.address = response.wallet
			req.sign = neg1.sign(response.sign)
			doc.walletAddress = response.wallet
			doc.walletSign = response.sign

			try {
				withTimeout(Duration((0.5 * Duration.Second).toLong()).milliSecond()) {
					val (res, err) = getGuardValue(req)
					if (err != null) {
						return@withTimeout
					}

					if (res.result == GetGuardValueResponse.Result.InvalidSign.int) {
						return@withTimeout
					}

					if (res.guardValue.isEmpty()) {
						doc.guardValue = neg1.encrypt(RustLib.sssY1(byte0, byteNeg1))
						return@withTimeout
					}

					if (res.guardValue.isNotEmpty()) {
						try {
							val decrypted = neg1.decrypt(res.guardValue)
							if (decrypted.isEmpty()) {
								return@withTimeout
							}
							doc.guardValue = res.guardValue
                        } catch (e: Exception) {
							return@withTimeout
						}
					}
				}
			} catch (_: TimeoutCancellationException) {
				break
			}
		}
	} while (false)
	if (doc.insert() != null) {
		doc.createTime = Date().time
		if (doc.update(Account.AllColumns()) != null) {
			return
		}
	}

	getUs().clone(doc.address)
	getUs().nc.postToMain(Account.ChangedEvent())


	val request = AskDCircleSignatureRequest(doc.address)
	request.items.add(
		AskDCircleSignatureRequest.Item(
			"",
			DCircle_ADDRESS,
			OpCode.AuthorizedAccessDCircle,
			AskDCircleSignatureRequest.Payload.New(Pair("Address", doc.address))
		)
	)

	val nonce = GetNonceSus(address = request.fromEthAddress, onlyDB = true) ?: return

	val results = GetSignResult(request.items.map {
		require(it.payload.valid) {"payload is invalid, please use Payload.New Build Object."}
		return@map GetSignResultItem(it.toEthAddress, it.opcode, it.payload.value, it.actionId ?: "")
	}.toTypedArray(), nonce, request.fromEthAddress)
	for (i in results.indices) {
		request.items[i].TxnHash = results[i]?.getTxnHash().toString()
		results[i]?.let { request.items[i].result = it }
	}

	val response = AskDCircleSignatureResponse(AskDCircleSignatureResponse.Code.SUCCESS)
	response.aes = Account.getPassword(demoPassword)
	response.fromEthAddress = request.fromEthAddress
	response.results = request.items.map { it.result }.toMutableList()

	Account.findByAddress(doc.address)?.let {
		doc.mnemonic =
			response.aes.decrypt(HexUtils.fromHex(it.mnemonic)).toString(Charsets.UTF_8)
		if (loginAccount(doc) != null) {
			return@let
		}
	}
}

