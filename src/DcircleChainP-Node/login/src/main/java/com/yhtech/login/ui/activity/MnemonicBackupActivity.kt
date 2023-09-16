package com.yhtech.login.ui.activity

import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewbinding.ViewBinding
import com.alibaba.android.arouter.facade.annotation.Route
import com.base.foundation.Aes
import com.base.foundation.BaseActivity
import com.base.foundation.DCircleScope
import com.base.foundation.db.Account
import com.base.foundation.db.findByAddress
import com.base.foundation.utils.fromHex
import com.base.thridpart.constants.Constants
import com.base.thridpart.constants.Router
import com.base.thridpart.setOnClickDelay
import com.yhtech.login.adapter.MnemonicAdapter
import com.yhtech.login.databinding.ActivityMnemonicBackupBinding
import kotlinx.coroutines.launch


@Route(path = Router.Login.loginMnemonicBackup)
class MnemonicBackupActivity : BaseActivity() {
    private lateinit var binding:ActivityMnemonicBackupBinding

    override fun initPageUi() {
        binding.cvBackup.setOnClickDelay {
            finish()
        }
        binding.ivBack.setOnClickDelay {
            finish()
        }

      DCircleScope.launch {
        loadFromDB()
      }
    }


    private fun splitMnemonic(mnemonic:String):List<String>{
        return mnemonic.trim().split(" ")
    }

    private suspend fun loadFromDB(){
      getMnemonic()?.let { mnemonic ->
        binding.tvAddress.text = address
        val data = splitMnemonic(mnemonic)
        binding.rvMnemonic.layoutManager = GridLayoutManager(this@MnemonicBackupActivity,3)
        val adapter = MnemonicAdapter()
        adapter.setList(data)
        binding.rvMnemonic.setOnTouchListener { v, event -> true }
        binding.rvMnemonic.adapter = adapter
        return
      }

      finish()
    }


  private val aesKey:String get() {
    return intent.getBundleExtra(Constants.BUNDLE)?.getString("AES_KEY")?: ""
  }

  private val address:String
  get() {
    return intent.getBundleExtra(Constants.BUNDLE)?.getString("Address")?:""
  }

    private suspend fun getMnemonic():String?{
     val account = Account.findByAddress(address) ?: return  null
      val aes = Aes(aesKey)
        val value = account.mnemonic
        if(value.isEmpty()){
          return null
        }
        try {
            val mnemonic = aes.decrypt(fromHex(value))
            return String(mnemonic)
        } catch (_:java.lang.Exception) {
        }
        return null
    }


    override fun initViewBinding(): ViewBinding {
       binding = ActivityMnemonicBackupBinding.inflate(layoutInflater)
        return binding
    }
}