package com.base.foundation.utils


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PromiseKt {

    fun <Value> runCoroutine( resolve: Promise.Resolve<Value>, value: Value) {
        CoroutineScope(Dispatchers.Main).launch {
            runTask(resolve,value)
        }

    }

    fun  runCoroutine( resolve: Promise.Reject, value: java.lang.Error) {
        CoroutineScope(Dispatchers.Main).launch {
            runError(resolve,value)
        }

    }

    //下面两个为挂起函数等待执行完毕切换为主线程
    private suspend fun<Value> runTask(resolve: Promise.Resolve<Value>, value: Value){
        withContext(Dispatchers.IO){
            resolve.run(value)
        }
    }

    private suspend fun runError(resolve: Promise.Reject, value: java.lang.Error){
        withContext(Dispatchers.IO){
            resolve.run(value)
        }
    }


}