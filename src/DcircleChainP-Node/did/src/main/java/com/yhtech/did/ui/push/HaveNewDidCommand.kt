package com.yhtech.did.ui.push

import android.os.Build
import android.util.Log
import com.anywithyou.stream.registerStreamPush
import com.base.foundation.DCircleScope
import com.base.foundation.getUs
import com.blankj.utilcode.util.DeviceUtils
import com.google.gson.Gson
import com.yhtech.did.ui.DIDArticleState
import com.yhtech.did.ui.api.getDIDArticle
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class HaveNewDidCommand {
    var seq: Int = 0
}

class DIDStateChange {
    var didAddress:String=""
    var didState:Int = 0
    var device:String = ""
    val deleteCode:Int = 0
}

fun registerPushDIDStateChange(){
    val allRunning: ConcurrentHashMap<String, AtomicBoolean> = ConcurrentHashMap()
    val allNext: ConcurrentHashMap<String, AtomicBoolean> = ConcurrentHashMap()

    registerStreamPush(getUs().nf.get().name,PushCmd.DIDStateChange.value,DIDStateChange::class.java){ data->
        Log.d(DIDArticleState::class.java.simpleName, "DIDStateChange data=${Gson().toJson(data)}")
        val key = data.didAddress

        // 创建或获取 running 和 next 标志
        val running: AtomicBoolean = synchronized(allRunning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                allRunning.computeIfAbsent(key) { AtomicBoolean(false) }
            } else {
                allRunning[key] ?: run {
                    allRunning[key] = AtomicBoolean(false)
                    allRunning[key]!!
                }
            }
        }
        val next: AtomicBoolean = synchronized(allNext) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                allNext.computeIfAbsent(key) { AtomicBoolean(false) }
            } else {
                allNext[key] ?: run {
                    allNext[key] = AtomicBoolean(false)
                    allNext[key]!!
                }
            }
        }
        // 避免并发处理正在运行时
        if (!running.compareAndSet(false, true)) {
            next.set(true)
            return@registerStreamPush
        }

        DCircleScope.launch {
            try {
                do {
                    next.set(false)
                    if (data.deleteCode>0 && data.device!=DeviceUtils.getUniqueDeviceId()) {
//                        DeleteArticle(data.didAddress, data.deleteCode)
                    }
                    if (data.device != DeviceUtils.getUniqueDeviceId()) {
                        getDIDArticle(arrayOf(data.didAddress))
                    }
                } while (next.get())
            } finally {
                running.set(false)
                allRunning.remove(key)
                allNext.remove(key)
            }
        }
    }
}

fun registerPushHaveNewDidCommand() {
    val allRunning: ConcurrentHashMap<String, AtomicBoolean> = ConcurrentHashMap()
    val allNext: ConcurrentHashMap<String, AtomicBoolean> = ConcurrentHashMap()

    registerStreamPush(getUs().nf.get().name,PushCmd.HaveNewDidCommand.value,HaveNewDidCommand::class.java){ data->

        val key = PushCmd.HaveNewDidCommand.value

        // 创建或获取 running 和 next 标志
        val running: AtomicBoolean = synchronized(allRunning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                allRunning.computeIfAbsent(key) { AtomicBoolean(false) }
            } else {
                allRunning[key] ?: run {
                    allRunning[key] = AtomicBoolean(false)
                    allRunning[key]!!
                }
            }
        }
        val next: AtomicBoolean = synchronized(allNext) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                allNext.computeIfAbsent(key) { AtomicBoolean(false) }
            } else {
                allNext[key] ?: run {
                    allNext[key] = AtomicBoolean(false)
                    allNext[key]!!
                }
            }
        }
        // 避免并发处理正在运行时
        if (!running.compareAndSet(false, true)) {
            next.set(true)
            return@registerStreamPush
        }

        DCircleScope.launch {
            try {
                do {
                    next.set(false)
                    val max = getPullDidCommandMaxSeq()
                    if (data.seq < max) {
                        return@launch
                    }
                    pullDidCommand()
                } while (next.get())
            } finally {
                running.set(false)
                allRunning.remove(key)
                allNext.remove(key)
            }
        }
    }

}
enum class PushCmd(val value: String) {
    HaveNewDidCommand("HaveNewDidCommand"),
    AddDIDSecretKeySuccess("AddDIDSecretKeySuccess"),
    DIDStateChange("DIDStateChange")
}