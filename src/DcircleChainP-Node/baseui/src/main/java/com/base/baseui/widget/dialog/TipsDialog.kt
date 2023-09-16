package com.base.baseui.widget.dialog

import android.content.Context
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import com.base.baseui.databinding.DialogTipsBinding
import com.base.thridpart.setOnClickDelay
import com.base.thridpart.setVisible
import com.blankj.utilcode.util.SizeUtils



class TipsDialog(context: Context, private var title_:CharSequence, var content:CharSequence, var confirmText:CharSequence?=null, private var cancelText:CharSequence?=null
                 , private var colorCancel:Int?=null, var colorConfirm:Int?=null, private var confirmClickable:Boolean = true, private var callBackClose:(isConfirm:Boolean)->Unit):
    BaseDialog(context) {

    lateinit var binding: DialogTipsBinding

    override fun initLayout(): ViewBinding {
        binding = DialogTipsBinding.inflate(layoutInflater)
        return binding
    }

    override fun initAfterShow() {
        super.initAfterShow()
        binding.apply {
            setCanceledOnTouchOutside(false)
            binding.tvTitle.text = title_
            binding.tvContent.text = content
            confirmText?.apply {
                binding.btnConfirm.text = confirmText
            }
            cancelText?.apply {
                binding.btnCancel.text = cancelText
            }
            binding.tvContent.setVisible(!content.isNullOrEmpty())
            binding.tvTitle.setVisible(!title_.isNullOrEmpty())
            if (!binding.tvContent.isVisible){
                (binding.tvTitle.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = SizeUtils.dp2px(11f)
            }
            if (cancelText.isNullOrBlank()){
                binding.btnCancel.setVisible(false)
                binding.buttonDivider.setVisible(false)
            }
            colorCancel?.apply {
                binding.btnCancel.setTextColor(this)
            }
            colorConfirm?.apply {
                binding.btnConfirm.setTextColor(this)
            }
            //点击关闭
            btnCancel.setOnClickDelay {
                //关闭对话框
                callBackClose.invoke(false)
                dismiss()
            }
            btnConfirm.setOnClickDelay {
              if (confirmClickable){
                  callBackClose.invoke(true)
                  dismiss()
              }
            }
        }
    }

    override fun setWindowPadding(window: Window) {
        window.decorView.setPadding(
            SizeUtils.dp2px(48f),
            0,
            SizeUtils.dp2px(48f),
            0
        )
    }


}
