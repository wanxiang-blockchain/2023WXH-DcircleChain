package com.yhtech.login


import android.util.Log
import com.base.thridpart.toHexString
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet
import java.math.BigInteger


fun getMnemonicForWallet(mnemonic:String):WalletMnemonic {
    val wallet = HDWallet(mnemonic,"")
    Log.d("getMnemonicForWallet", wallet.mnemonic())
    val key = wallet.getKeyForCoin(CoinType.ETHEREUM)
    val walletMnemonic = WalletMnemonic()
    walletMnemonic.address = wallet.getAddressForCoin(CoinType.ETHEREUM)
    walletMnemonic.privateKey = key.data().toHexString()
    walletMnemonic.publicKey = key.getPublicKeySecp256k1(true).data().toHexString()
    return walletMnemonic
}

class WalletMnemonic{
    var privateKey:String = ""
    var publicKey:String = ""
    var sign:String = ""
    var signHash:String = ""
    var message:String = ""
    var address:String = ""
    var nonce :BigInteger = BigInteger.valueOf(0)

    override fun toString(): String {
        return "$privateKey $publicKey $sign"
    }
}
