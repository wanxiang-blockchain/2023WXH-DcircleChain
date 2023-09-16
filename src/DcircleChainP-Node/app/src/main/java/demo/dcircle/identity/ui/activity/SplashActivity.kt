package demo.dcircle.identity.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.anywithyou.stream.Duration
import com.base.baseui.widget.LoadingDialog
import com.yhtech.did.ui.api.GetDIDMeCreated
import com.base.baseui.widget.ui.AskDCircleSignatureRequest
import com.base.baseui.widget.ui.AskDCircleSignatureResponse
import com.base.foundation.DCircleScope
import com.base.foundation.NetScope
import com.base.foundation.PPKey
import com.base.foundation.R
import com.base.foundation.RustLib
import com.base.foundation.TPWalletSignMessage
import com.base.foundation.api.GetNetConfig
import com.base.foundation.api.GetShareConfig
import com.base.foundation.api.RestoreTheLatest
import com.base.foundation.api.SyncDIDBrowser
import com.base.foundation.base.AppRouter
import com.base.foundation.api.CodeError
import com.base.foundation.chain.DCircle_ADDRESS
import com.base.foundation.chain.GetNonceSus
import com.base.foundation.chain.GetSignResult
import com.base.foundation.chain.GetSignResultItem
import com.base.foundation.chain.OpCode
import com.base.foundation.db.Account
import com.base.foundation.db.AllColumns
import com.base.foundation.db.BIP39
import com.base.foundation.db.findByAddress
import com.base.foundation.db.getPassword
import com.base.foundation.db.Init
import com.base.foundation.db.Me
import com.base.foundation.db.insert
import com.base.foundation.db.setValue
import com.base.foundation.db.update
import com.base.foundation.demoMnemonic
import com.base.foundation.demoPackageName
import com.base.foundation.demoPassword
import com.base.foundation.getUs
import com.base.foundation.getWalletKey
import com.base.foundation.setUs
import com.base.foundation.setWalletKey
import com.base.foundation.ts.RegisterAutoLogin
import com.base.foundation.utils.toHex
import com.base.thridpart.constants.Router
import com.base.foundation.utils.HexUtils.fromHex
import com.google.gson.Gson
import com.gyf.immersionbar.ImmersionBar
import com.yhtech.login.api.getGuardValue
import com.yhtech.login.api.GetGuardValueRequest
import com.yhtech.login.api.GetGuardValueResponse
import com.yhtech.login.api.loginAccount
import com.yhtech.login.getMnemonicForWallet
import com.yhtech.login.api.ethPersonalSign
import com.yhtech.login.api.newMnemonic
import com.yhtech.login.api.newNeg1Mnemonic
import demo.dcircle.identity.databinding.ActivitySplashBinding
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import wallet.core.jni.HDWallet
import wallet.core.jni.Mnemonic
import java.util.Date


class SplashActivity : AppCompatActivity() {

	private lateinit var binding: ActivitySplashBinding
	private val loginFrom:String? get() {
		return intent.getStringExtra("loginFrom")
	}

	private fun initPageUi() {
		ImmersionBar.with(this)
			.autoDarkModeEnable(true)
			.navigationBarColor(R.color.white)
			.fitsSystemWindows(true)
			.init()

		val ppk = getWalletKey()
		Log.d("SplashActivity", "initPageUi ppk=${Gson().toJson(ppk)}")

		DCircleScope.launch {
			if (ppk.isValid()) {
				setUs(getUs().clone(ppk.address))

				RegisterAutoLogin(getUs().nf.get(), login = suspend {
					DCircleScope.launch {
						Me.setValue(Me.me, Me.empty)
						getUs().nf.get().clear401()
						login(demoMnemonic)
					}
					CodeError("自动处理 401，暂未实现")
				})

				AppRouter.route(this@SplashActivity, Router.Main.mainPage)
				authorization()
				finish()
				return@launch
			}

			val us = RestoreTheLatest()
			if (us==null) {
				login(demoMnemonic)
				return@launch
			}

			RegisterAutoLogin(getUs().nf.get(), login = suspend {
				DCircleScope.launch {
					Me.setValue(Me.me, Me.empty)
					getUs().nf.get().clear401()
					login(demoMnemonic)
				}
				CodeError("自动处理 401，暂未实现")
			})

			val info = getMnemonicForWallet(demoMnemonic)
			val ppk = PPKey()
			ppk.privateKey = info.privateKey
			ppk.publicKey = info.publicKey
			ppk.address = info.address

			setWalletKey(ppk)
			AppRouter.route(this@SplashActivity, Router.Main.mainPage)

			authorization()

			finish()
		}

		NetScope.launch {
			val job0 = async {
				BIP39.Init()
			}
			val job1 = async {
				GetShareConfig()
			}

			val job2 = async {
				GetNetConfig()
			}

			val job3 = async {
				SyncDIDBrowser()
			}

			listOf(job0, job1, job2, job3).awaitAll()
		}
	}

	private suspend fun login(words: String) {
		val checkM = Mnemonic.isValid(words)
		if (!checkM){
			AppRouter.route(this@SplashActivity, Router.Main.mainPage)
			finish()
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
				getString(R.string.account_management_sign_method),
				DCircle_ADDRESS,
				OpCode.AuthorizedAccessDCircle,
				AskDCircleSignatureRequest.Payload.New(Pair("Address", doc.address))
			)
		)

		val loadingDialog = LoadingDialog(this)

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
				response.aes.decrypt(fromHex(it.mnemonic)).toString(Charsets.UTF_8)
			if (loginAccount(doc) != null) {
				loadingDialog.dismiss()
				return@let
			}
			GetDIDMeCreated()

			loadingDialog.dismiss()

			AppRouter.route(this@SplashActivity, Router.Main.mainPage)

			authorization()

			finish()
		}
	}

	private fun authorization() {
		// 此处由授权 socail 跳转过来
		loginFrom?.apply {
			Log.d("SplashActivity", "initPageUi loginFrom=${this}")
			if (this==demoPackageName) {
				AppRouter.route(this@SplashActivity, Router.DeApps.authorization)
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivitySplashBinding.inflate(layoutInflater)
		setContentView(binding.root)

		Log.d("SplashActivity", "onCreate")

		initPageUi()
	}
}