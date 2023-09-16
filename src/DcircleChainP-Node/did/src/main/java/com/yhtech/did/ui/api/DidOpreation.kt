package com.yhtech.did.ui.api

import com.base.foundation.db.DIDArticle
import com.base.foundation.db.findByAddress
import com.base.foundation.getUs


suspend fun checkDidPermission(address:String):Boolean{
    var ar = DIDArticle.findByAddress(address)
    if (ar?.CreatorUid.isNullOrEmpty()||ar==null){
        getDIDArticle(arrayOf(address))
        ar = DIDArticle.findByAddress(address)
    }
    if (ar?.CreatorUid== getUs().getUid()){
        return true
    }
    //已购买
    return ar?.PayStatus==DIDArticle.EPayStatus.Paid.int
}