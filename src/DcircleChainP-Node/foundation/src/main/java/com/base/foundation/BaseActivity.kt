package com.base.foundation

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.base.foundation.nc.ObserverAble
import java.lang.reflect.Method

/**
 * 基类activity，只处理ui相关的共同特性
 */
abstract class BaseActivity:AppCompatActivity(), ObserverAble {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (needCheckOrientation()) {
            val fixOrientation: Boolean = fixOrientation()
        }
        super.onCreate(savedInstanceState)
        setContentView(initViewBinding().root)

        initPageUi()
    }

    abstract fun initPageUi()

    abstract fun initViewBinding():ViewBinding


    private fun needCheckOrientation(): Boolean {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()
    }

    override fun setRequestedOrientation(requestedOrientation: Int) {
        if (needCheckOrientation()) {
            return
        }
        super.setRequestedOrientation(requestedOrientation)
    }

    private fun isTranslucentOrFloating(): Boolean {
        var isTranslucentOrFloating = false
        try {
            val styleableRes = Class.forName("com.android.internal.R\$styleable")
                .getField("Window")[null] as IntArray
            val ta = obtainStyledAttributes(styleableRes)
            val m: Method = ActivityInfo::class.java.getMethod(
                "isTranslucentOrFloating",
                TypedArray::class.java
            )
            m.isAccessible = true
            isTranslucentOrFloating = m.invoke(null, ta) as Boolean
            m.isAccessible = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isTranslucentOrFloating
    }

    private fun fixOrientation(): Boolean {
        try {
            val field = Activity::class.java.getDeclaredField("mActivityInfo")
            field.isAccessible = true
            val o = field.get(this) as ActivityInfo
            o.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            field.isAccessible = false
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    override fun getName(): String {
        return this::class.java.name
    }

}