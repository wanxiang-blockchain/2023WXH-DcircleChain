package com.anywithyou.stream

/**
 * 处理java调用kotlin的中间件
 * 按实际情况添加对应函数
 */
object JavaToKotlinMiddleWare {

    /**
     * 翻译pushCallback
     */
    fun paresPushCallback(data: ByteArray) {
        handlerOfPush("main").invoke(String(data))
    }

}