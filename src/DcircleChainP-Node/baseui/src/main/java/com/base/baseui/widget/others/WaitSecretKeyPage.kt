package com.base.baseui.widget.others

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.base.baseui.R
import com.base.baseui.databinding.LockViewBinding
import com.base.baseui.widget.dialog.WaitUnLockDialog
import com.blankj.utilcode.util.ActivityUtils

class WaitSecretKeyPage(context: Context,attributeSet: AttributeSet?=null):FrameLayout(context,attributeSet){
    var binding = LockViewBinding.inflate(LayoutInflater.from(context),this,true)
    private var waitDialog = WaitUnLockDialog(context)
    init {
        val frameLayoutparams = LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT)
        layoutParams = frameLayoutparams
    }

}