package com.base.baseui.widget

import com.base.foundation.db.FindByKey
import com.base.foundation.db.KeyVal
import java.text.SimpleDateFormat
import java.util.*


suspend fun getDIDLastStateTime():String{
    KeyVal.FindByKey(KeyVal.Keys.DIDLastStateTime.toString())?.apply {
        val format = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH)
        return format.format(this.Value.toLong())
    }

    return "0000/00/00 00:00"
}

suspend fun getDIDLastStateDate():String{
    KeyVal.FindByKey(KeyVal.Keys.DIDLastStateTime.toString())?.apply {
        val format = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
        return format.format(this.Value.toLong())
    }

    return "0000/00/00"
}