package com.base.foundation.oss

import com.base.foundation.sendSus
import com.base.foundation.db.*
import com.base.foundation.getUs
import com.base.foundation.utils.Tuple
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.util.*

class BucketInfo {
    var endpoint: String = ""

    @SerializedName("bucketName")
    var bucket: String = ""
}

class GetBucketInfoResponse {
    var bucketInfo: BucketInfo = BucketInfo()
}

suspend fun GetBucketInfo(): Tuple<BucketInfo, Error?> {
    val key = KeyVal.buildKeyForUid(getUs().getUid())
    KeyVal.FindByKey(key)?.apply {
        if (this.ExpireTime > Date().time) {
            try {
                val data = Gson().fromJson(this.Value, BucketInfo::class.java)
                if (data!=null && data.bucket.isNotEmpty() && data.endpoint.isNotEmpty()) {
                    return Tuple(data, null)
                }
            } catch (_:Exception) { }
        }
    }


    val (ret, err) = sendSus("/oss/sts/GetBucketInfo", object {}, GetBucketInfoResponse::class.java)
    if (err != null) {
        return Tuple(BucketInfo(), err)
    }

    val expireTime = Date().time + 24 * 60 * 60 * 1000

    val doc = KeyVal()
    doc.Key = key
    doc.Value = Gson().toJson(ret.bucketInfo)
    doc.ExpireTime = expireTime
    if (doc.insert()!=null) {
        doc.update()
    }

    return Tuple(ret.bucketInfo, null)
}