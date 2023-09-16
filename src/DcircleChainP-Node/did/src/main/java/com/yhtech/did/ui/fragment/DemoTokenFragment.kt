package com.yhtech.did.ui.fragment

import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.base.foundation.BaseFragment
import com.base.foundation.utils.MakeToast
import com.yhtech.did.R
import com.yhtech.did.databinding.FragmentDemoTokenBinding
import com.yhtech.did.ui.rv.DidToken
import com.yhtech.did.ui.rv.DidTokenAdapter

class DemoTokenFragment: BaseFragment() {
    lateinit var binding: FragmentDemoTokenBinding
    private lateinit var adapter:DidTokenAdapter
    override fun initViewBinding(): ViewBinding {
        binding = FragmentDemoTokenBinding.inflate(LayoutInflater.from(context),null,false)
        initPageView()
        return binding
    }

    override fun initPageView() {
        adapter = DidTokenAdapter()
        binding.rv.layoutManager = LinearLayoutManager(context)
        binding.rv.adapter = adapter
        binding.tvHopeIng.setOnClickListener {
            MakeToast.showShort("敬请期待")
        }

        val btc = DidToken(R.mipmap.ic_token_btc,"BTC",0,0)
        val eth = DidToken(R.mipmap.ic_token_eth,"ETH",0,0)
        val bnb = DidToken(R.mipmap.ic_token_bnb,"BNB",0,0)
        val op = DidToken(R.mipmap.ic_token_op,"OP",0,0)
        val dCircle = DidToken(R.mipmap.ic_token_dcircle,"DCIRCLE",0,0)
        val matic = DidToken(R.mipmap.ic_token_matic,"Matic",0,0)
        adapter.setList(listOf(btc,eth,bnb,op,dCircle,matic))
    }
}