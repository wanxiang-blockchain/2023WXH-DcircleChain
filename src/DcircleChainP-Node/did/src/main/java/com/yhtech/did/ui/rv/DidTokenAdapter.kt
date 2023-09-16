package com.yhtech.did.ui.rv

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yhtech.did.R
import com.yhtech.did.databinding.ItemDidTokenBinding

class DidTokenAdapter: BaseQuickAdapter<DidToken,BaseViewHolder>(R.layout.item_did_token) {

    @Override
    override fun convert(holder: BaseViewHolder, item: DidToken) {
        val binding = ItemDidTokenBinding.bind(holder.itemView)
        binding.imgLogo.setImageResource(item.res)
        binding.tvName.text = item.name
        binding.tvCount.text = "${item.count}"
        binding.tvPrice.text = "$ ${item.price}"
    }
}