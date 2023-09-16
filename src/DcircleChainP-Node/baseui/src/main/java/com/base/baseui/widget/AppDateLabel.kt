package com.base.baseui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.alibaba.android.arouter.launcher.ARouter
import com.base.baseui.databinding.AppDateLabelBinding
import com.base.foundation.DCircleScope
import com.base.foundation.GetDIDBrowser
import com.base.foundation.db.KeyVal
import com.base.foundation.getUs
import com.base.foundation.nc.ObserverAble
import com.base.thridpart.constants.Constants
import com.base.thridpart.constants.Router
import com.base.thridpart.setOnClickDelay
import com.base.thridpart.setUnderLine
import kotlinx.coroutines.launch

class AppDateLabel @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs),ObserverAble {

    val binding = AppDateLabelBinding.inflate(LayoutInflater.from(context), this, true)
    private var uid = getUs().getUid()
    private var gotoUrl = ""

    init {
        gravity = Gravity.CENTER
        orientation = HORIZONTAL

        binding.tvDcirclescan.setOnClickDelay {
            var url = GetDIDBrowser().GetUserUrl(uid)
            if (gotoUrl.isNotEmpty()) {
                url = gotoUrl
            }
            ARouter.getInstance()
                .build(Router.Did.webActivity)
                .withString(Constants.PARAM, url)
                .navigation()

        }
        binding.tvDcirclescan.setUnderLine()
        binding.tvVersion.text = "ver.01"

        DCircleScope.launch {
            binding.tvDate.text = getDIDLastStateTime()
        }
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        getUs().nc.addObserver(this, KeyVal.ChangedEvent(KeyVal.Keys.DIDLastStateTime.toString())) {
            DCircleScope.launch {
                binding.tvDate.text = getDIDLastStateTime()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        getUs().nc.removeAll(this)
    }
}