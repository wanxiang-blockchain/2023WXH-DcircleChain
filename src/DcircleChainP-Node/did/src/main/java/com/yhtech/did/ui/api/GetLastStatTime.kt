package com.yhtech.did.ui.api

import com.anywithyou.stream.Duration
import com.base.foundation.DCircleScope
import com.base.foundation.sendSus
import com.base.foundation.db.FindByKey
import com.base.foundation.db.KeyVal
import com.base.foundation.db.insert
import com.base.foundation.db.update
import com.base.foundation.getUs
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.max

class GetLastStatTimeRequest

class GetLastStatTimeResponse {
    var lastStatTime = 0L
}

private suspend fun getLastStatTime() {
    val (ret, err) = sendSus("/im/chat/GetLastStatTime", GetLastStatTimeRequest(), GetLastStatTimeResponse::class.java)
    if (err != null) {
        return
    }

    if (ret.lastStatTime<=0) {
        return
    }

    val keyVal = KeyVal()
    keyVal.Key = KeyVal.Keys.DIDLastStateTime.toString()
    keyVal.Value = ret.lastStatTime.toString()
    if (keyVal.insert()!=null) {
        keyVal.update()
    }

    getUs().nc.postToMain(KeyVal.ChangedEvent(KeyVal.Keys.DIDLastStateTime.toString()))

    return
}

var timer:Timer? = null
suspend fun GetLastStatTime(){
    getLastStatTime()

    var delay = Duration(3*Duration.Minute).milliSecond()
    KeyVal.FindByKey(KeyVal.Keys.DIDLastStateTime.toString())?.apply {
        val expired = this.Value.toLong() + Duration(3*Duration.Minute).milliSecond()
        delay = max(expired - Date().time, Duration(Duration.Minute).milliSecond())
    }

    timer?.cancel()
    timer = Timer()
    timer?.schedule(object : TimerTask() {
        override fun run() {
            DCircleScope.launch {
                GetLastStatTime()
            }
        }
    }, delay)

    return
}