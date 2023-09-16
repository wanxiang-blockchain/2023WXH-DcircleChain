package com.base.baseui.widget.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.viewbinding.ViewBinding
import com.base.baseui.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog


/**
 * 底部弹起dialog并追加软键盘基类
 */
abstract class KeyboardBottomDialog(context:Context, style:Int=R.style.StyleBottomDialog, private var layoutDialogParams:ViewGroup.LayoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)):BottomSheetDialog(context,style
) {

    private lateinit var contentView: View
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentView = initViewBinding().root
        setContentView(contentView,layoutDialogParams)
    }
    abstract fun initViewBinding():ViewBinding

    open fun beforeShow(){

    }


    open fun afterShow(){
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    open fun showDialog(){
        //设置无title与背景透明
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setDimAmount(0.2f)

        beforeShow()
        show()
        afterShow()
    }
}