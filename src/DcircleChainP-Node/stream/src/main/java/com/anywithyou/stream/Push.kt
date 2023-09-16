package com.anywithyou.stream

import android.util.Log
import com.google.gson.Gson
import org.json.JSONObject

class Command {
    var cmd: String = ""
    var data: RawJson = RawJson()
}

typealias NetName = String
typealias Cmd = String


val allPushHandlers = mutableMapOf<NetName, MutableMap<Cmd, (data: String) -> Unit>>()

fun <T> registerStreamPush(net: String, cmd: Cmd, clazz: Class<T>, handler: (data: T) -> Unit) {
    println("RegisterStreamPush $cmd for ${net}")
    val cmds = allPushHandlers.getOrPut(net) { mutableMapOf() }
    cmds[cmd] = { data ->
        try {
            val res = Gson().fromJson(data, clazz)
            handler.invoke(res as T)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun handlerOfPush(net: String): (data: String) -> Unit {


    return { data ->
        val cmds = allPushHandlers[net]
        try {

            val cmd = Gson().fromJson(data,Command::class.java)
            val jsonObject = JSONObject(data)
            var strdata = ""
            if (jsonObject.has("data")){
                strdata = jsonObject.getJSONObject("data").toString()
            }
            if (cmd.cmd.isEmpty()) {
                println("'cmd' in push data is error for $net")
            }
            val handler = cmds?.get(cmd.cmd)
            if (handler == null) {
                println("not register push handler for ${cmd.cmd} of $net")

            }

            if (cmd.data == null) {
                println("'data' in push data is null for ${cmd.cmd} of $net")

            }
            if (handler != null) {
                Log.e("推送原始数据${cmd.cmd}",strdata)
                handler(strdata)
            }
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }
    }
}
