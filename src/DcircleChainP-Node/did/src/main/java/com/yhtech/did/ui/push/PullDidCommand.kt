package com.yhtech.did.ui.push

import android.util.Log
import com.base.foundation.sendSus
import com.base.foundation.db.Get
import com.base.foundation.db.SetValue
import com.base.foundation.db.Syncer
import com.blankj.utilcode.util.LogUtils
import com.google.gson.Gson
import com.yhtech.did.ui.api.setDIDPurchaserSecretKey
import kotlin.math.max

enum class DidCmd(val value: Int) {
    DIDNeedSetSecretKey(1),
}

fun getCmdTypeByInt(value: Int): DidCmd {
    return when (value) {
        1 -> DidCmd.DIDNeedSetSecretKey
        else -> DidCmd.DIDNeedSetSecretKey
    }
}

class CmdDidNeedSetSecretKeyInfo {
    var uid: String = ""
    var done: Boolean = false
}

interface DidCmdProcessor {
    suspend fun run(data: String, didAddress: String): Error?
}

class DidNeedSetSecretKeyProcessor : DidCmdProcessor {
    override suspend fun run(data: String, didAddress: String): Error? {
        val ret = Gson().fromJson(data, CmdDidNeedSetSecretKeyInfo::class.java)
        LogUtils.d("DidNeedSetSecretKeyProcessor", didAddress, data)
        if (ret.done) {
            return null
        }
        setDIDPurchaserSecretKey(didAddress, ret.uid)
        return null
    }

}

object DidCmdProcessorFactory {
    private val map = mapOf<DidCmd, () -> DidCmdProcessor>(
        DidCmd.DIDNeedSetSecretKey to { DidNeedSetSecretKeyProcessor() },
    )

    fun build(cmd: DidCmd): DidCmdProcessor {
        return map[cmd]?.invoke() ?: object : DidCmdProcessor {
            override suspend fun run(data: String, didAddress: String): Error {
                return Error("not support cmd $cmd")
            }
        }
    }
}

class PullDidCommandRequest {
    var limit: Int = 100
    var start: Int = 0
}

class PullDidCommandResponseItem {
    var didAddress: String = ""
    var cmd: Int = 0
    var data: String = ""
    var seq: Int = 0
}

class PullDidCommandResponse {
    var items: List<PullDidCommandResponseItem> = emptyList()
}
fun SyncerPullDidCmdKey():String{
    return "PullDidCommand"
}
suspend fun getPullDidCommandMaxSeq(): Int {
    return Syncer.Get(SyncerPullDidCmdKey())
}

suspend fun pullDidCommand() {
    val request = PullDidCommandRequest()
    request.limit = 1000
    val start = getPullDidCommandMaxSeq()
    request.start = start
    val (ret, err) = sendSus("/im/chat/PullDidCommand", request, PullDidCommandResponse::class.java)
    if (err != null) {
        Log.e("PullDidCommand", "err=${err}")
        return
    }

    val syncer = Syncer()
    syncer.key = SyncerPullDidCmdKey()
    var maxSeq = 0
    for (item in ret.items) {
        maxSeq = max(maxSeq, item.seq)
        LogUtils.d("pullDidCommand cmd ${item.cmd}", item.didAddress, item.data)
        val processor = DidCmdProcessorFactory.build(getCmdTypeByInt(item.cmd))
        val err1 = processor.run(item.data, item.didAddress)
        if (err1 != null) {
            LogUtils.w("pullDidCommand cmd ${item.cmd}", item.didAddress, item.data, "processor error", err1)
            return
        }
    }

    syncer.value = maxSeq
    syncer.SetValue()

    if (ret.items.size != request.limit) {
        return
    }

    return pullDidCommand()
}