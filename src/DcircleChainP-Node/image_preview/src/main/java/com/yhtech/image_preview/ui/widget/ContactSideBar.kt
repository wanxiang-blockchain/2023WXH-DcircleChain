package com.yhtech.image_preview.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView

/**
 * 侧边栏
 */
class ContactSideBar(context: Context,attrs: AttributeSet) : View(context, attrs) {
    private var choose = -1 // 选中
    private val paint = Paint()
    private var mTextDialog: TextView? = null
    //触摸事件
    private var onTouchingLetterChangedListener: OnTouchingLetterChangedListener? = null


    /**
     * 重写这个方法
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 获取焦点改变背景颜色.
        val height = height // 获取对应高度
        val width = width // 获取对应宽度
        var singleHeight = height * 1f / indexes.size // 获取每一个字母的高度
        singleHeight = (height * 1f - singleHeight / 2) / indexes.size
        for (i in indexes.indices) {
            paint.color = Color.rgb(86, 86, 86)
            paint.typeface = Typeface.DEFAULT
            paint.isAntiAlias = true
            paint.textSize = 23f

            // x坐标等于中间-字符串宽度的一半.
            val xPos = width / 2 - paint.measureText(indexes[i]) / 2
            val yPos = singleHeight * i + singleHeight
            canvas.drawText(indexes[i], xPos, yPos, paint)
            paint.reset() // 重置画笔
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        val y = event.y // 点击y坐标
        val oldChoose = choose
        val listener = onTouchingLetterChangedListener
        val c = (y / height * indexes.size).toInt() // 点击y坐标所占总高度的比例*b数组的长度就等于点击b中的个数.
        when (action) {
            MotionEvent.ACTION_UP -> {
                background =  ColorDrawable(0x00000000)
                choose = -1 //
                invalidate()
                if (mTextDialog != null) {
                    mTextDialog!!.visibility = INVISIBLE
                }
            }
            else -> {
                background = ColorDrawable(0x13161316)
                if (oldChoose != c) {
                    if (c >= 0 && c < indexes.size) {
                        listener?.onTouchingLetterChanged(indexes[c])
                        if (mTextDialog != null) {
                            mTextDialog!!.text = indexes[c]
                            mTextDialog!!.visibility = VISIBLE
                        }
                        choose = c
                        invalidate()
                    }
                }
            }
        }
        return true
    }


    /**
     * 接口
     *
     * @author coder
     */
    interface OnTouchingLetterChangedListener {
        fun onTouchingLetterChanged(s: String?)
    }

    companion object {
        // 26个字母
        var indexes = arrayOf(
           "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q",
            "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"
        )
    }
}