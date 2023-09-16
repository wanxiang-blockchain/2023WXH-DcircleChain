package com.yhtech.did.ui.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import com.base.baseui.widget.dialog.SignDialog
import com.base.baseui.widget.dialog.SignDialogVerify
import com.base.baseui.widget.others.TabViewPagerAdapter
import com.base.foundation.Aes
import com.base.foundation.DCircleScope
import com.base.foundation.base.AppRouter
import com.base.foundation.db.Account
import com.base.foundation.db.getPassword
import com.base.foundation.demoPassword
import com.base.foundation.getUs
import com.base.thridpart.constants.Router
import com.google.android.material.tabs.TabLayout
import com.gyf.immersionbar.ImmersionBar
import com.yhtech.did.databinding.FragmentDemoDidBinding
import com.yhtech.image_preview.R
import kotlinx.coroutines.launch

class DemoDidFragment:Fragment() {
    lateinit var binding: FragmentDemoDidBinding
    private var fragmentList = mutableListOf<Fragment>()
    private var mPageTitleList = mutableListOf<String>()
    private var demoContentFragment = DemoContentFragment()
    private var demoTokenFragment = DemoTokenFragment()
    private var demoConfirmBinding = DemoConfirmFragment()
    private lateinit var fragmentAdapter:TabViewPagerAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDemoDidBinding.inflate(inflater,container,false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPager()
    }

    private fun initViewPager() {
        ImmersionBar.with(this@DemoDidFragment)
            .statusBarColor(R.color.transparent)
            .navigationBarColor(R.color.color_f9f9f9)
            .autoDarkModeEnable(true)
            .autoStatusBarDarkModeEnable(true)
            .statusBarDarkFont(true)
            .init()
        binding.tvCardBackup.setOnClickListener {
            DCircleScope.launch {
                SignDialogVerify(getUs().getUid(), object : SignDialog.Listener {
                    override suspend fun onSuccess(dialog: SignDialog?, aes: Aes) {
                        mnemonicBackup()
                    }

                    override fun onCancel() {
                    }

                    override fun onError(err: Error) {
                    }

                })
            }
        }
        binding.tvCardText3.text = getUs().getUid()
        fragmentList.add(demoContentFragment)
        fragmentList.add(demoTokenFragment)
        fragmentList.add(demoConfirmBinding)
        mPageTitleList.add(getString(R.string.content))
        mPageTitleList.add("Token")
        mPageTitleList.add("已授权")
        fragmentAdapter = TabViewPagerAdapter(childFragmentManager, fragmentList, mPageTitleList)
        binding.recordVp.adapter = fragmentAdapter
        val onGlobalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.tablayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                for (i in 0 until binding.tablayout.tabCount) {
                    binding.tablayout.getTabAt(i)?.view?.isLongClickable = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        binding.tablayout.getTabAt(i)?.view?.tooltipText = null
                    }
                }
            }
        }
        binding.tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.tablayout.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
        binding.tablayout.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
        binding.tablayout.setupWithViewPager(binding.recordVp)
    }

    private fun mnemonicBackup() {
        val bundle = Bundle()
        val aes = Account.getPassword(demoPassword)
        bundle.putString("AES_KEY", aes.key)
        bundle.putString("Address", getUs().getUid())
        context?.let { it1 ->
            AppRouter.withBundle(bundle)
                .route(it1, Router.Login.loginMnemonicBackup)
        }
    }
}