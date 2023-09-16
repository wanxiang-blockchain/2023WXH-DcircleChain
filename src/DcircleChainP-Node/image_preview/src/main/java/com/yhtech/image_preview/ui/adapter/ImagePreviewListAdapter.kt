package com.yhtech.image_preview.ui.adapter

import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.luck.picture.lib.tools.ScreenUtils
import com.yhtech.image_preview.R
import com.yhtech.image_preview.databinding.ItemImgPreviewBinding
import com.yhtech.image_preview.ui.Im
import com.yhtech.image_preview.ui.ImageWhHandle
import com.yhtech.image_preview.ui.widget.AvatarImage

class ImagePreviewListAdapter:BaseQuickAdapter<Im.MsgImageContent,BaseViewHolder>(R.layout.item_img_preview) {


    override fun convert(holder: BaseViewHolder, item: Im.MsgImageContent) {
        val itemBinding = ItemImgPreviewBinding.bind(holder.itemView)
        val (w, h) = ImageWhHandle.scaleImage(context, item.large.width, item.large.height)
        val nWidth = ScreenUtils.getScreenWidth(context)
        val nHeight = nWidth*h/w

        val large = AvatarImage().apply {
            this.objectId = item.large.objectId
            this.key = item.large.key
        }
        val origin = AvatarImage().apply {
            this.objectId = item.original.objectId
            this.key = item.original.key
        }
        val thumb = AvatarImage().apply {
            this.objectId = item.thumb.objectId
            this.key = item.thumb.key
        }

        if (h>w && nHeight > ScreenUtils.getScreenHeight(context)){
            itemBinding.pictureLongView.isVisible = true
            itemBinding.pictureView.isVisible = false
            itemBinding.pictureLongView.setWidthHeight(nWidth,nHeight)
            itemBinding.pictureLongView.SetImage(origin, large, thumb)
        }else{
            itemBinding.pictureLongView.isVisible = false
            itemBinding.pictureView.isVisible = true
            itemBinding.pictureView.setWidthHeight(nWidth,nHeight)
            itemBinding.pictureView.SetImage(origin, large, thumb)
        }


    }

}