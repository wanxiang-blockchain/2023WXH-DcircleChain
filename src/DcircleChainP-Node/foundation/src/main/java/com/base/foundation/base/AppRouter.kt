package com.base.foundation.base

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.lang.ref.WeakReference

/**
 * 处理跨模块页面跳转，交由appmoudle
 */
object AppRouter {
    private var liveDateRouter:MutableLiveData<RouterBean> = MutableLiveData<RouterBean>()
    var context:WeakReference<Context>?=null //避免内存泄露，使用弱引用
    private var nowRouter: RouterBean?=null
    private var observer:Observer<RouterBean>?=null

    /**
     * 在appmoudle下使用
     */
    fun initObserver(observer: Observer<RouterBean>){
        AppRouter.observer = observer
        //如果重复注册，需要移除
        if (liveDateRouter.hasObservers()){
            liveDateRouter.removeObserver(observer)
        }
        liveDateRouter.observeForever(observer)
    }
    /**
     * 自定义路由跳转
     */
    private fun goToPage(){
        if (liveDateRouter.hasActiveObservers()){
            nowRouter?.apply {
                liveDateRouter.value = this
            }

        }
    }

    private fun checkRouter(){
        if (nowRouter ==null){
            nowRouter = RouterBean()
        }
    }

    /**
     * 跳转直接使用此函数
     * @param context 避免直接使用application跳转
     */
    fun route(context: Context,routePath:String){
        AppRouter.context = WeakReference(context)
        checkRouter()
        nowRouter?.routePath = routePath
        goToPage()
    }

    /**
     * 参数配置
     */
    fun withParams(value:String): AppRouter {
        checkRouter()
        nowRouter?.routeParam = value
        return this
    }

    fun withParams(key:String,value:String): AppRouter {
        checkRouter()
        nowRouter?.routeKey = key
        nowRouter?.routeParam = value
        return this
    }


    /**
     * 传多参数建议使用此
     */
    fun withBundle(bundle: Bundle): AppRouter {
        checkRouter()
        nowRouter?.bundle = bundle
        return this
    }

    /**
     * 防止泄漏清除引用
     */
    fun clear() {
        nowRouter =null
        context =null
    }


}


/**
 * @param routeParam 参数，建议用string转换
 * @param routeKey 参数key，不传默认为Constants.PARAM
 * @param routePath 需要给appmodule的路由地址
 */
data class RouterBean(
    var routePath:String="",
    var routeParam:String="",
    var routeKey:String="",
    var bundle: Bundle?=null,
)