package com.base.baseui.widget.dialog

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.viewbinding.ViewBinding
import com.base.baseui.R
import com.base.baseui.databinding.DialogSignBinding
import com.base.foundation.Aes
import com.base.foundation.DCircleScope
import com.base.foundation.ShowTouchID
import com.base.foundation.db.Account
import com.base.foundation.db.findByAddress
import com.base.foundation.db.getPassword
import com.base.foundation.demoPassword
import com.base.foundation.getUs
import com.base.foundation.utils.MakeToast
import com.base.foundation.utils.fromHex
import com.base.thridpart.setVisible
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ViewUtils
import kotlinx.coroutines.launch

class SignDialog (context: Context, private var fromEthAddress: String = getUs().getUid(), var listener: Listener):
    KeyboardBottomDialog(context) {
    lateinit var binding: DialogSignBinding

    override fun initViewBinding(): ViewBinding {
        binding = DialogSignBinding.inflate(LayoutInflater.from(context),null,false)
        binding.tvDemoPwd.text = "Demo演示密码：${demoPassword}"
        initClick()
        return binding
    }

    private fun initClick(){
        binding.tvCancel.setOnClickListener {
            listener.onCancel()
            dismiss()
        }
        binding.imgFingerprint.setOnClickListener {
            showTouchId()
        }
        binding.tvExit.setOnClickListener {
            confirmClick()
        }
        showTouchId()
    }

    private fun showTouchId(){
        DCircleScope.launch {
            ShowTouchID(ActivityUtils.getTopActivity(),fromEthAddress)?.apply {
                dismiss()
                listener.onSuccess(null, this)
                return@launch
            }
            TipsDialog(context, context.getString(R.string.create_identity_no_fingerprint_title),context.getString(
                R.string.create_identity_no_fingerprint_illustrate),context.getString(R.string.picture_know), colorConfirm = context.resources.getColor(
                R.color.blue_1183ff,null)){}.showDialog()
            listener.onCancel()
        }
    }
    private fun confirmClick(){
        if (binding.etEnterPassword.text.toString().isEmpty()) {
            Toast.makeText(context,context.getString(R.string.enter_password), Toast.LENGTH_SHORT).show()
            return
        }
        DCircleScope.launch {
            checkPassword(Account.getPassword(binding.etEnterPassword.text.toString()))
        }
    }

    private suspend fun checkPassword(aes: Aes) {
        val account:Account = Account.findByAddress(fromEthAddress)?:throw Exception("can not find account(${fromEthAddress}), please check you logic.")
        val value = account.mnemonic
        if(value.isEmpty()){
            binding.tvPasswordDes.setVisible(true)
            binding.tvPasswordDes.text = ActivityUtils.getTopActivity()?.getString(R.string.password_not_correct)
            return
        }
        try {
            val mnemonic = aes.decrypt(fromHex(value))
            if(mnemonic == null || mnemonic.isEmpty() ){
                MakeToast.showShort(R.string.password_not_correct)
                return
            }
            binding.tvPasswordDes.setVisible(false)
            dismiss()
            listener.onSuccess(null,aes)
        } catch (e:java.lang.Exception) {
            ViewUtils.runOnUiThread {
                binding.tvPasswordDes.setVisible(true)
                binding.tvPasswordDes.text = ActivityUtils.getTopActivity()?.getString(R.string.password_not_correct)
            }
        }
    }

    interface Listener {
        suspend fun onSuccess(dialog:SignDialog?, aes: Aes)
        fun onCancel()
        fun onError(err:Error)
    }

}

suspend fun SignDialogVerify(fromEthAddress:String, listener: SignDialog.Listener) {
    val account = Account.findByAddress(fromEthAddress)
    if (account==null) {
        listener.onError(Error("fromEthAddress(${fromEthAddress}) not found"))
        return
    }
    ActivityUtils.getTopActivity()?.let {
        SignDialog(
            it,
            fromEthAddress,
            listener
        ).showDialog()
    }
}