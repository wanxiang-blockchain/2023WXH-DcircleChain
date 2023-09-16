package com.yhtech.login.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yhtech.login.R
import com.yhtech.login.databinding.ItemMenmonicBinding


class MnemonicAdapter: BaseQuickAdapter<String,BaseViewHolder>(R.layout.item_menmonic) {

    override fun convert(holder: BaseViewHolder, item: String) {
        val binding = ItemMenmonicBinding.bind(holder.itemView)
        binding.tvMnemonic.text = item
        binding.tvCardOrder.text = (holder.layoutPosition+1).toString()
        binding.root.setBackgroundResource(getSelectBg(holder.layoutPosition))
    }


    private fun getSelectBg(itemType: Int):Int{

        return when(itemType){
            0->{
                R.drawable.shape_left_top_moinc
            }
            2->{
                R.drawable.shape_right_top_monic
            }
            9->{
                R.drawable.shape_bottom_left
            }
            11->{
                R.drawable.shape_bottom_right
            }
            else->{
                R.drawable.shape_trunk_moinc
            }
        }
    }

}