package com.yhtech.did.ui.push

import com.anywithyou.stream.registerStreamPush
import com.base.foundation.db.DIDArticle
import com.base.foundation.getUs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PushCmdAddDIDSecretKeySuccess{
    var didAddress:String = ""
}

fun registerPushAddDIDSecretKeySuccess() {
    registerStreamPush(getUs().nf.get().name,
        PushCmd.AddDIDSecretKeySuccess.value, PushCmdAddDIDSecretKeySuccess::class.java){ data->
        CoroutineScope(Dispatchers.Main).launch {
            val address =  mutableListOf(data.didAddress)

            getUs().nc.postToMain(DIDArticle.AddSecretKeySuccessEvent(data.didAddress))
            DIDArticle.AddSecretKeySuccessEvent().apply {
                this.ids = address
                getUs().nc.postToMain(this)
            }
        }
    }
}