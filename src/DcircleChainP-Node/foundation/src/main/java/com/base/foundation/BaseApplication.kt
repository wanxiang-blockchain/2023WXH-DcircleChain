package com.base.foundation

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.alibaba.android.arouter.launcher.ARouter
import com.tencent.bugly.crashreport.CrashReport

open class BaseApplication:Application() , Application.ActivityLifecycleCallbacks {
    companion object{
        @JvmField
         var id = ""
        @JvmField
         var type = RadarCameraType.Community
    }

    enum class RadarCameraType(val type: String) {
        Community("community"), User("user"),
    }


    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        setAppContext(this)
        try {
            ARouter.init(this)
        }catch (e:Exception){
            throw Error(e)
        }

        ARouter.openDebug()
        ARouter.openLog()
        CrashReport.initCrashReport(applicationContext, "008a2d4607", false)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log.d(this::class.java.simpleName, "页面 " + activity::class.java.simpleName + " onActivityCreated")
    }

    override fun onActivityStarted(activity: Activity) {
        Log.d(this::class.java.simpleName, "页面 " + activity::class.java.simpleName + " onActivityStarted")
    }

    override fun onActivityResumed(activity: Activity) {
        Log.d(this::class.java.simpleName, "页面 " + activity::class.java.simpleName + " onActivityResumed")
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.d(this::class.java.simpleName, "页面 " + activity::class.java.simpleName + " onActivityDestroyed")
    }


    override fun onTerminate() {
        unregisterActivityLifecycleCallbacks(this)
        super.onTerminate()
    }
}