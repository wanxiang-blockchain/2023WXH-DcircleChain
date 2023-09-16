package com.base.foundation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.base.foundation.nc.ObserverAble

abstract  class BaseFragment:Fragment(), ObserverAble {
    override fun getName(): String {
        return this::class.java.simpleName
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return initViewBinding().root
    }
    abstract fun initViewBinding():ViewBinding

    abstract fun initPageView()

}