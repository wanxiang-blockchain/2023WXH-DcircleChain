package com.base.baseui.widget.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.viewbinding.ViewBinding
import com.base.baseui.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.gyf.immersionbar.ImmersionBar


/**
 * 底部弹起dialog基类
 * 只提供初始化和规范化流程
 * 具体业务逻辑子类实现
 */
abstract class BaseBottomSheetDialog(context:Context, style:Int=R.style.BottomSheetDialog
                                     ,
                                     private var layoutDialogParams:ViewGroup.LayoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)):BottomSheetDialog(context,style
) {

    private lateinit var contentView: View
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentView = initViewBinding().root
        setContentView(contentView,layoutDialogParams)
        contentView.setPadding(0, 0, 0, ImmersionBar.getNavigationBarHeight(context))
        // 获取 BottomSheetBehavior 实例并保存到变量中
        bottomSheetBehavior = BottomSheetBehavior.from(contentView.parent as View)

        val window = window
        window?.let {
            val layoutParams = it.attributes
            layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            it.attributes = layoutParams
        }
    }


    abstract fun initViewBinding():ViewBinding


    open fun beforeShow(){

    }


    open fun afterShow(){
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

}