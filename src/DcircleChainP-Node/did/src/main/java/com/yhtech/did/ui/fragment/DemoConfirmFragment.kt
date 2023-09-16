package com.yhtech.did.ui.fragment

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.alibaba.android.arouter.launcher.ARouter
import com.base.baseui.widget.others.radar.RadarType
import com.base.foundation.BaseFragment
import com.base.foundation.DCircleScope
import com.base.foundation.getUs
import com.base.thridpart.constants.Router
import com.yhtech.did.databinding.FragmentDemoConfirmBinding
import com.yhtech.did.ui.api.getDIDArticleStat
import kotlinx.coroutines.launch

class DemoConfirmFragment: BaseFragment() {
    lateinit var binding: FragmentDemoConfirmBinding

    override fun initViewBinding(): ViewBinding {
        binding = FragmentDemoConfirmBinding.inflate(LayoutInflater.from(context),null,false)
        initPageView()
        return binding
    }

    override fun initPageView() {
        DCircleScope.launch {
            loadFromServer()
            binding.authorizeAccess.setOnClickListener {
                ARouter.getInstance().build(Router.DeApps.authorization).navigation()
            }
            binding.tvChooseServer.setOnClickListener {
                ARouter.getInstance().build(Router.DeApps.chatauth).navigation()
            }
            binding.radarView.setRadar(RadarType.User, getUs().getUid())
        }
    }

    private suspend fun loadFromServer() {
        getDIDArticleStat(getUs().getUid())
    }
}