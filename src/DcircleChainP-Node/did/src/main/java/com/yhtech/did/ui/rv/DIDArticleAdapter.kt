package com.yhtech.did.ui.rv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alibaba.android.arouter.launcher.ARouter
import com.base.baseui.widget.utils.SubText
import com.base.foundation.DCircleScope
import com.base.foundation.db.DIDBlockMetaContentNode
import com.base.foundation.db.DIDBlockMetaNode
import com.base.thridpart.constants.Constants
import com.base.thridpart.constants.Router
import com.base.thridpart.setOnClickDelay
import com.base.thridpart.setVisible
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.luck.picture.lib.ShowIntoView
import com.luck.picture.lib.ShowIntoViewListener
import com.luck.picture.lib.tools.ScreenUtils
import com.luck.picture.lib.widget.longimage.SubsamplingScaleImageView
import com.yhtech.did.R
import com.yhtech.did.databinding.ItemRvDidArticleWaterfallBinding
import com.yhtech.image_preview.databinding.ItemOssImageViewBinding
import com.yhtech.did.ui.api.checkDidPermission
import kotlinx.coroutines.launch

class DIDArticleAdapter(private var owner: Owner) : BaseMultiItemQuickAdapter<DIDBlockMetaContentNode, BaseViewHolder>(){
    interface Owner {
        suspend fun onImageNodeClick(position:Int)
    }
    private var curAddress:String = ""
    init {
        addItemType(DIDBlockMetaNode.Type.Image.int,R.layout.item_oss_image_view)
        addItemType(DIDBlockMetaNode.Type.Article.int,R.layout.item_rv_did_article_waterfall)
        addItemType(DIDBlockMetaNode.Type.Unknown.int,R.layout.item_rv_did_unknown)
    }

    fun setCurDidAddress(address:String) {
        curAddress = address
    }

    override fun convert(holder: BaseViewHolder, item: DIDBlockMetaContentNode) {
        if (holder is MyDidViewHolder) {
            holder.setData(item, curAddress)
        } else if (holder is MyViewHolder) {
            holder.setData(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when(viewType) {
            DIDBlockMetaNode.Type.Article.int -> {
                MyDidViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_rv_did_article_waterfall, null)
                )
            }
            DIDBlockMetaNode.Type.Image.int -> {
                MyViewHolder(owner,
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_oss_image_view, null)
                )
            }
            DIDBlockMetaNode.Type.Unknown.int ->{
                UnKnownViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_rv_did_unknown,parent,false)
                )
            }
            else ->{
                super.onCreateViewHolder(parent, viewType)
            }
        }
    }

    class UnKnownViewHolder(itemView: View):BaseViewHolder(itemView)

    class MyViewHolder(var owner:Owner, itemView: View) : BaseViewHolder(itemView) {
        private lateinit var node:DIDBlockMetaContentNode
        fun setData(item: DIDBlockMetaContentNode) {
            if (::node.isInitialized && node.hash == item.hash) {
                return
            }

            node = item
            val binding = ItemOssImageViewBinding.bind(itemView)
            val layoutParams = binding.clCl.layoutParams
            layoutParams.width = ScreenUtils.getScreenWidth(ActivityUtils.getTopActivity()) - SizeUtils.dp2px(40f)
            layoutParams.height = item.height*layoutParams.width/item.width
            binding.clCl.layoutParams = layoutParams

            DCircleScope.launch {
                ShowIntoView(item.objectId, item.objectKey, binding.imgOssView, listener = object :
                    ShowIntoViewListener() {
                    override fun onImageLoad(view: SubsamplingScaleImageView) {
                        binding.imgPlaceHolder.setVisible(false)
                    }

                    override fun config(view: SubsamplingScaleImageView) {
                        view.isZoomEnabled = false
                        view.isPanEnabled = false
                        view.setDoubleTapZoomScale(1.0f)
                        view.setOnClickDelay {
                            DCircleScope.launch {
                                owner.onImageNodeClick(bindingAdapterPosition)
                            }
                        }
                    }
                })
            }
        }
    }

    class MyDidViewHolder(itemView: View): BaseViewHolder(itemView) {
        fun setData(item:DIDBlockMetaContentNode, curAddress:String) {
            val binding = ItemRvDidArticleWaterfallBinding.bind(itemView)
            val layoutParams = binding.llContent.layoutParams
            layoutParams.width = ScreenUtils.getScreenWidth(ActivityUtils.getTopActivity()) - SizeUtils.dp2px(40f)
            layoutParams.height = SizeUtils.dp2px(133f)
            binding.llContent.layoutParams = layoutParams
            val ref = item.ref
            ref?.apply {
                binding.tvAddress.text = SubText.shortenString(didAddress)
                binding.tvName.text = title
                binding.tvDesc.text = abstractText
                val img = abstractImages?.get(0)
                img?.apply {
                    key?.let { objectId?.let { it1 -> binding.avatarView.setContent(it1, it) } }
                }
                itemView.setOnClickListener {
                    DCircleScope.launch {
                        if (checkDidPermission(didAddress)) {
                            ARouter.getInstance().build(Router.Did.didContent)
                                .withString(Constants.PARAM, didAddress)
                                .withString("fromDidAddress", curAddress)
                                .navigation()

                            return@launch
                        }
                        ARouter.getInstance().build(Router.Did.didSignature)
                            .withString(Constants.PARAM, didAddress)
                            .withString("fromDidAddress", curAddress)
                            .navigation()
                    }
                }
            }

        }
    }
}