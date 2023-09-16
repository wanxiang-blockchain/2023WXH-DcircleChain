package com.base.baseui.widget.dialog

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.viewbinding.ViewBinding
import com.base.baseui.R
import com.base.thridpart.setOnClickDelay
import com.blankj.utilcode.util.SizeUtils


/**
 * @author: dsy
 * @date: 2021/7/30
 * @desc：
 */
abstract class BaseDialog(context: Context) : Dialog(context, R.style.NormalDialog) {
    var root: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyCompat()
        root = initLayout().root
        setContentView(root!!)
        setWindowAttributes()
        setCanceledOnTouchOutside(setCanceledOnTouchOutside())

    }
    private fun applyCompat() {
        if (Build.VERSION.SDK_INT < 19) {
            return
        }
        window!!.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    /**
     * 点击空白关闭键盘
     **/
    open fun registerInputTouchAction() {
        try {
            val mInputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            root?.setOnClickDelay {
                mInputMethodManager.apply {
                    currentFocus.apply {
                        mInputMethodManager.hideSoftInputFromWindow(
                            currentFocus?.windowToken,
                            0
                        )
                    }
                }
            }

        } catch (e: Exception) {

        }
    }

    abstract fun initLayout(): ViewBinding

    open fun showDialog() {
        initBeforeShow()
        show()
        initAfterShow()
    }

    //需要在ui初始化之前的部分
    open fun initBeforeShow() {
    }

    //ui初始化之后的部分
    open fun initAfterShow() {
        registerInputTouchAction()
    }

    open fun setWindowAttributes() {
        window?.apply {
            setWindowAnimations(setDialogAnimation()) //设置窗口弹出动画
            setBackgroundDrawableResource(R.color.transparent) //设置对话框背景为透明
            setGravity(setDialogGravity())
            setWindowPadding(this)
            val lp = attributes
            lp.x = 0
            lp.y = 0
            setWidthAndHeight(lp)
            attributes = lp
        }
    }

    open fun setWidthAndHeight(lp: WindowManager.LayoutParams) {
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
    }

    open fun setWindowPadding(window: Window) {
        window.decorView.setPadding(
            SizeUtils.dp2px(38f),
            0,
            SizeUtils.dp2px(38f),
            0
        )
    }

    open fun setDialogAnimation() = R.style.baseDialog

    open fun setDialogGravity() = Gravity.CENTER

    open fun setCanceledOnTouchOutside() = false
}