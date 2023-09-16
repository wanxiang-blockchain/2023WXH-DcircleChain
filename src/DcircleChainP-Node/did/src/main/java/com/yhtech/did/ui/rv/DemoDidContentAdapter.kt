package com.yhtech.did.ui.rv

import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.base.foundation.DCircleScope
import com.base.foundation.GetDIDBrowser
import com.base.foundation.db.DIDArticle
import com.base.foundation.db.DIDBlockMetaNode
import com.base.foundation.db.GetDIDArticleARMeta
import com.base.foundation.db.GetDIDArticleEncMeta
import com.base.foundation.utils.MakeToast
import com.base.thridpart.constants.Constants
import com.base.thridpart.constants.Router
import com.base.thridpart.setOnClickDelay
import com.base.thridpart.setVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yhtech.did.R
import com.yhtech.did.databinding.ItemDemoDidContentBinding
import kotlinx.coroutines.launch

class DemoDidContentAdapter: BaseQuickAdapter<DIDArticle, MyViewHolder>(R.layout.item_demo_did_content) {

    override fun convert(holder: MyViewHolder, item: DIDArticle) {
            holder.setData(item)
    }

}
class MyViewHolder(itemView: View) : BaseViewHolder(itemView) {
    fun setData(item: DIDArticle) {
        val binding = ItemDemoDidContentBinding.bind(itemView)
        if (item.Address == "add") {
            binding.icDemoGlid.setVisible(false)
            binding.tvDidAddress.setVisible(false)
            binding.tvDidName.text = "添加"
            binding.viewLockImg.setVisible(false)
            binding.demoDidImg.setImageResource(R.mipmap.ic_demo_add)
            binding.viewDataBind.setOnClickDelay {
                MakeToast.showShort("敬请期待")
            }
        }else {
            binding.icDemoGlid.setVisible(true)
            binding.tvDidAddress.setVisible(true)
            binding.viewLockImg.setVisible(true)
            binding.tvDidAddress.text = item.Address
            DCircleScope.launch {
                GetDIDArticleEncMeta(item)?.apply {
                    binding.tvDidName.text = this.Title.text
                }

                GetDIDArticleARMeta(item)?.apply {
                    this@apply.Content.find { it.itemType == DIDBlockMetaNode.Type.Image.int }
                        ?.apply {
                            binding.demoDidImg.setContent(this.objectId, this.objectKey)
                        }
                }
                binding.viewAllGlid.setOnClickListener {
                    ARouter.getInstance()
                        .build(Router.Did.webActivity)
                        .withString(Constants.PARAM, GetDIDBrowser().GetArticleUrl(item.Address))
                        .navigation()
                }
                binding.viewDataBind.setOnClickListener {
                    ARouter.getInstance().build(Router.Did.didContent)
                        .withString(Constants.PARAM, item.Address)
                        .navigation()
                }
            }


        }
    }
}