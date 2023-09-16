package com.base.baseui.widget.others

import android.content.Context
import android.content.res.Resources
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.base.baseui.R

/**
 * @author yangfei
 * @time 2023/2/17
 * @desc
 */
open class GilroyTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defSyle: Int = 0
) : AppCompatTextView(context, attrs, 0) {
    private val fontType: String?

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.GilroyTextView)
        fontType = typedArray.getString(R.styleable.GilroyTextView_gilroyFont)
        init(fontType)
    }

    /***
     * 设置字体
     *
     * @return
     */
    fun init(fontType: String?) {
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = when (fontType) {
            "0" -> {0.9f}
            "1" -> {0.8f}
            "2" -> {0.7f}
            "3" -> {dp2px(0.7f)}
            "4" -> {dp2px(0.4f)}
            "5" -> {dp2px(0.015f)}
            "6" -> {0.3f}
            "7" -> {0.2f}
            else -> {dp2px(0.015f)}
        }
    }

    open fun dp2px(dpValue: Float): Float {
        val scale = Resources.getSystem().displayMetrics.density
        return (dpValue * scale)
    }

    fun setFontType(fontType: String?){
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth =  when (fontType) {
            "0" -> {0.9f}
            "1" -> {0.8f}
            "2" -> {0.7f}
            "3" -> {dp2px(0.7f)}
            "4" -> {dp2px(0.4f)}
            "5" -> {dp2px(0.015f)}
            "6" -> {0.3f}
            "7" -> {0.2f}
            else -> {dp2px(0.015f)}
        }
        invalidate()
    }
}