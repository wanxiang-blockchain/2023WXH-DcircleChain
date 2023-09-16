package com.base.baseui.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import com.base.baseui.databinding.LoadingDialogBinding
import com.base.thridpart.setVisible
import com.blankj.utilcode.util.SizeUtils

class LoadingDialog(context: Context) : Dialog(context) {


    private lateinit var binding: LoadingDialogBinding

    private var msg: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoadingDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        val lp = window?.attributes
        lp?.width = SizeUtils.dp2px(120F)
        lp?.height = SizeUtils.dp2px(120F)
        window?.attributes = lp

        if (!msg.isNullOrEmpty()) {
            binding.tvText.setVisible(true)
            binding.tvText.text = msg
        }

        val animation = RotateAnimation(
            0F,
            360F,
            Animation.RELATIVE_TO_SELF,
            0.5F,
            Animation.RELATIVE_TO_SELF,
            0.5F
        )

        animation.duration = 1000
        animation.repeatCount = Animation.INFINITE
        animation.interpolator = LinearInterpolator()
        animation.fillAfter = true

        binding.ivProgress.animation = animation
        animation.start()
    }

    fun setText(text:String){
        msg = text
        if (isShowing){
            binding.tvText.setVisible(true)
            binding.tvText.text = msg
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding.ivProgress.clearAnimation()
    }


}