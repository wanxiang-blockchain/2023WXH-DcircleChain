package dcircle.identity.deapps.ui.activity

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.alibaba.android.arouter.facade.annotation.Route
import com.base.baseui.widget.ui.askDCircleSignature
import com.base.baseui.widget.ui.AskDCircleSignatureListener
import com.base.baseui.widget.ui.AskDCircleSignatureRequest
import com.base.baseui.widget.ui.AskDCircleSignatureResponse
import com.base.foundation.BaseActivity
import com.base.foundation.DCircleScope
import com.base.foundation.R
import com.base.foundation.chain.DCircle_ADDRESS
import com.base.foundation.chain.OpCode
import com.base.foundation.db.findByAddress
import com.base.foundation.getUs
import com.base.foundation.getWalletKey
import com.base.foundation.utils.MakeToast
import com.base.thridpart.constants.Router
import com.google.gson.Gson
import dcircle.identity.deapps.databinding.ActivityAuthorizationBinding
import kotlinx.coroutines.launch

@Route(path = Router.DeApps.authorization)
class AuthorizationActivity: BaseActivity() {
    private lateinit var binding:ActivityAuthorizationBinding

    override fun initPageUi() {
        binding.tvJoinGroup.setOnClickListener {
            DCircleScope.launch {
                askSignature()
            }
        }
        binding.ivBack.setOnClickListener { finish() }
    }

    override fun initViewBinding(): ViewBinding {
        binding = ActivityAuthorizationBinding.inflate(LayoutInflater.from(this),null,false)
        return binding
    }

    private suspend fun askSignature() {

        if (!isSocialInstalled()) {
            MakeToast.showShort("请安装Dcircle Social")
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://chain.yunhuaitech.com")
            startActivity(intent)
            return
        }

        val req = AskDCircleSignatureRequest()
        req.fromEthAddress = getUs().getUid()
        req.items.add(
            AskDCircleSignatureRequest.Item(
                getString(R.string.account_management_sign_method), DCircle_ADDRESS,
                OpCode.AuthorizedAccessDCircle,
                AskDCircleSignatureRequest.Payload.New(
                    Pair("Authorized application", "Dcircle Social"),
                    Pair("Authorization data", "Dcircle Social")
                )
            ))

        askDCircleSignature(req,object: AskDCircleSignatureListener {
            override suspend fun onResponse(response: AskDCircleSignatureResponse) {
                if (response.code == AskDCircleSignatureResponse.Code.CANCEL) {
                    return
                }

                if (response.code == AskDCircleSignatureResponse.Code.FAIL) {
                    return
                }
                response.results.apply {
                    if (isNotEmpty()) {
                        link()
                    }
                }
            }
        })
    }

    private suspend fun link() {
        val intent = Intent()
        intent.component =
            ComponentName("demo.web3.social", "demo.web3.social.ui.activity.SplashActivity")

        val ppk = getWalletKey()
        intent.putExtra("ppKey", Gson().toJson(ppk))
        intent.putExtra("token",getUs().nf.get().getToken())
        com.base.foundation.db.Account.findByAddress(getUs().getUid())?.apply {
            intent.putExtra("account", Gson().toJson(this))
        }
        startActivity(intent)
    }

    private fun isSocialInstalled(): Boolean {
        val socialPackageName = "demo.web3.social"
        val packageManager = packageManager
        val intent = packageManager.getLaunchIntentForPackage(socialPackageName)
        return intent != null
    }
}