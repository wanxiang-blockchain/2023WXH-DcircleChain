package com.example.myapplication.ui.login.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import com.example.myapplication.MainActivity
import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.myapplication.fundation.db.DCWalletDataBase
import com.example.myapplication.fundation.wallet.PolygonWallet
import com.example.myapplication.fundation.wallet.User
import com.example.myapplication.ui.BaseActivity
import java.io.File


class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var walletPath:File

    override fun onCreate(savedInstanceState: Bundle?) {
        StrictMode.setThreadPolicy(
            ThreadPolicy.Builder().detectNetwork().penaltyLog()
                .build()
        )

        super.onCreate(savedInstanceState)
        walletPath = this.filesDir
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val words = binding.words

        var me = DCWalletDataBase.getUserDao(this.applicationContext)?.me()
        if( me != null ) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.createWallet?.setOnClickListener(View.OnClickListener {
            binding.words?.isEnabled = false
            val wallet:PolygonWallet  = PolygonWallet.createWallet(this.applicationContext,"")
            Log.d(
                "ActivityLoginBinding: createWallet ",
                "mnemonic:${wallet.getMnemonic()} \n file: ${wallet.getWalletFilePath()}"
            )
            binding.words?.setText(wallet.getMnemonic())
            login(wallet)
        })

        binding.importWallet?.setOnClickListener(View.OnClickListener {
            var wallet: PolygonWallet = PolygonWallet.createWalletFromMnemonic(this.applicationContext,words.toString(),"")
            Log.d(
                "ActivityLoginBinding: importWallet ",
                "mnemonic:${wallet.getMnemonic()} \n file: ${wallet.getWalletFilePath()}"
            )
            login(wallet)
        })

//        binding.connectWallet?.setOnClickListener(View.OnClickListener {
////            var wallet: PolygonWallet = PolygonWallet.createWalletFromMnemonic(this.applicationContext,words.toString(),"")
////
////            Log.d(
////                "ActivityLoginBinding: connectWallet ",
////                "mnemonic:${wallet.getMnemonic()} \n file: ${wallet.getWalletFilePath()}"
////            )
////            login(wallet)
//            connectWallet()
//        })

    }

    fun connectWallet() {
    }
    fun login(wallet:PolygonWallet) {
        val intent = Intent(this, MainActivity::class.java)
        var user = User(wallet.getAddress(),wallet.getPrivateKey(),wallet.getMnemonic(),wallet.getWalletFilePath())
        DCWalletDataBase.getUserDao(applicationContext)?.insertAll(user)
        startActivity(intent)
    }

}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}