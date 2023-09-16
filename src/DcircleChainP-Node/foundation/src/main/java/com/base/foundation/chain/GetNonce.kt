package com.base.foundation.chain

import com.base.foundation.api.postJsonNoTokenSus
import com.base.foundation.api.Request
import com.base.foundation.api.Response
import com.base.foundation.db.FindByKey
import com.base.foundation.db.KeyVal
import com.base.foundation.db.Set
import com.base.foundation.getUs
import com.github.xpwu.ktdbtable.invoke
import com.google.gson.Gson
import java.math.BigInteger

class GetNonceRequest {
    var address:String = ""
}


class GetNonceResponse {
    var nonce: Long = 0
}

fun GetNonceKey(address:String = getUs().getUid()):String {
    return "${::GetNonceSus}_${address}"
}

suspend fun GetNonceSus(address:String = getUs().getUid(), onlyDB:Boolean = false):BigInteger? {
    val key = GetNonceKey(address)

    val keyVal = getUs().shareDB {
        KeyVal.FindByKey(it, key)
    }
    if (onlyDB && keyVal!=null) {
        return keyVal.Value.toBigInteger()
    }

    val request = GetNonceRequest()
    request.address = address
    val req = Request("",request)
    val (ret, err) = postJsonNoTokenSus<Response<GetNonceResponse>>("/chain/chain/GetNonce", req, getUs().nf.get(), Response(0, GetNonceResponse::class.java)::class.java)
    if (err!=null) {
        return null
    }

    val data = Gson().fromJson(Gson().toJson(ret.data), GetNonceResponse::class.java)

    getUs().shareDB {
        KeyVal.Set(it, key, data.nonce.toString())
    }

    return BigInteger.valueOf(data.nonce)
}