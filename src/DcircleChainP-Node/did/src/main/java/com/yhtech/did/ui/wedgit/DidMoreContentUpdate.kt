package com.yhtech.did.ui.wedgit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.yhtech.did.databinding.DidMoreItemHeaderBinding

class DidMoreContentUpdate(context: Context,set: AttributeSet?=null):FrameLayout(context,set) {
    val binding = DidMoreItemHeaderBinding.inflate(LayoutInflater.from(context),this,true)
}