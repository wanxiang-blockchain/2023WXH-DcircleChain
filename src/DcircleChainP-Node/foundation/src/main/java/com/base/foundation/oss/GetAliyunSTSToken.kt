package com.base.foundation.oss

import com.base.foundation.sendSus
import com.base.foundation.db.FindByKey
import com.base.foundation.db.KeyVal
import com.base.foundation.db.insert
import com.base.foundation.db.update
import com.base.foundation.utils.Tuple
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.util.*

class AliyunStsToken {
    var accessKeyId: String = ""
    var accessKeySecret: String = ""
    var expiration: Long = 0

    @SerializedName("securityToken")
    var stsToken: String = ""
}

class GetAliyunSTSTokenResponse {
    var result: Int = 0
    var stsToken: AliyunStsToken = AliyunStsToken()
}

suspend fun GetAliyunSTSToken(): Tuple<AliyunStsToken, Error?> {
    KeyVal.FindByKey(KeyVal.Keys.AliyunSTSToken.toString())?.apply {
        if (this.ExpireTime > Date().time) {
            try {
                val data = Gson().fromJson(this.Value, AliyunStsToken::class.java)
                if (data!=null && data.accessKeyId.isNotEmpty() && data.accessKeySecret.isNotEmpty() && data.stsToken.isNotEmpty()) {
                    return Tuple(data, null)
                }
            } catch (_:Exception) { }
        }
    }


    val (ret, err) = sendSus("/oss/sts/GetAliyunSTSToken", object {}, GetAliyunSTSTokenResponse::class.java)
    if (err != null) {
        return Tuple(AliyunStsToken(), Error(err.message))
    }

    val expireTime = ret.stsToken.expiration

    val keyVal = KeyVal()
    keyVal.Key = KeyVal.Keys.AliyunSTSToken.toString()
    keyVal.Value = Gson().toJson(ret.stsToken)
    keyVal.ExpireTime = expireTime
    if (keyVal.insert()!=null) {
        keyVal.update()
    }

    return Tuple(ret.stsToken, null)
}