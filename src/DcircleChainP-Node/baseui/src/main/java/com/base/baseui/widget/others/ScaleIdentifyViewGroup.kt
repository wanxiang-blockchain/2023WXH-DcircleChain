package com.base.baseui.widget.others

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.widget.FrameLayout
import com.base.baseui.widget.others.ScaleIdentifyViewGroup.MyScaleGestureDetector
import kotlin.math.abs


/**
 * 缩放识别的FrameLayout(继承viewGroup就行)
 * 总体思路
 * 1.首先拦截viewGroup的事件，通过[onInterceptTouchEvent],如果判断是2根手指则拦截,反之不拦截
 * 2.拦截后通过官方的[SimpleOnScaleGestureListener]方便的判断双指的移动情况
 * 3.只需要额外判断手势是放大还是缩小即可,核心的一点代码在[MyScaleGestureDetector.onScale]中
 */
class ScaleIdentifyViewGroup @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val mScaleGestureDetector by lazy {
        ScaleGestureDetector(
            context,
            MyScaleGestureDetector()
        )
    }

    private var mOnScaleListener: OnScaleListener? = null

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let {
            //如果是2根手指的手势则拦截,反之则不拦截
            return ev.pointerCount == 2
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (event == null) {
            super.onTouchEvent(event)
        } else {
            mScaleGestureDetector.onTouchEvent(event)
        }
    }

    /**
     * 双指识别
     */
    private inner class MyScaleGestureDetector : SimpleOnScaleGestureListener() {

        var changeSpan = 0f

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (changeSpan == 0f) {
                changeSpan = detector.previousSpan
            }
            //previousSpan是2个手指之前的平均距离  currentSpan是结束时手指的平均距离
            //判断一个阈值,手指的移动需要达到这个条件
            if (abs(changeSpan - detector.currentSpan) > 50) {
                //双指缩小
                if (changeSpan > detector.currentSpan) {
                    mOnScaleListener?.onShrink()
                } else {//双指扩张
                    mOnScaleListener?.onDilate()
                }
                changeSpan = detector.currentSpan
            }
            return super.onScale(detector)
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            super.onScaleEnd(detector)
            changeSpan = 0f
        }

    }

    /**
     * 设置监听
     */
    fun setScaleIdentifyListener(listener: OnScaleListener) {
        mOnScaleListener = listener
    }

    interface OnScaleListener {
        /**
         * 缩小
         */
        fun onShrink()

        /**
         * 扩张
         */
        fun onDilate()
    }

}