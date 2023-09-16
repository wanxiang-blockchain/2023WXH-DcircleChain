package com.yhtech.image_preview.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.LifecycleObserver
import com.base.foundation.DCircleScope
import com.base.foundation.nc.ObserverAble
import com.base.foundation.oss.OnMediaStateChangeListener
import com.base.foundation.oss.Priority
import com.base.foundation.oss.ShowIntoView
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class OSSImageView(context: Context, attr:AttributeSet?=null):RoundedImageView(context,attr),LifecycleObserver,ObserverAble {
    private var showBlur = false
    private var onMediaStateChangeListener:OnMediaStateChangeListener = OnMediaStateChangeListener()
    private var job:Job? = null

    fun setContent(objectId:String,key:String, channel:Priority = Priority.Channel4) {
        job?.cancel()
        job = DCircleScope.launch {
            ShowIntoView(objectId, key, this@OSSImageView,channel, listener = onMediaStateChangeListener, blur = showBlur)
        }
    }
}

