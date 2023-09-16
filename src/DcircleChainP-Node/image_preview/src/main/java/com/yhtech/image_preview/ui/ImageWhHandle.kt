package com.yhtech.image_preview.ui

import android.content.Context
import android.view.WindowManager
import com.base.foundation.utils.Tuple
import com.blankj.utilcode.util.ScreenUtils

class ImageWhHandle {

        companion object {
            fun scaleImage(context: Context, width: Int, height: Int): Tuple<Int, Int> {
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val screenWidth = ScreenUtils.getScreenWidth()
                val screenHeight = ScreenUtils.getScreenHeight()

                // 如果图片的宽度小于屏幕宽度，且图片高度大于屏幕高度，则为长图
                return if (width < screenWidth && height > screenHeight) {
                    val scaleRatio = height.toFloat() / screenHeight.toFloat()
                    val scaledWidth = (width / scaleRatio).toInt()
                    Tuple(scaledWidth, screenHeight)
                }
                // 如果图片的高度小于屏幕高度，且图片宽度大于屏幕宽度，则为宽图
                else if (height < screenHeight && width > screenWidth) {
                    val scaleRatio = width.toFloat() / screenWidth.toFloat()
                    val scaledHeight = (height / scaleRatio).toInt()
                    Tuple(screenWidth, scaledHeight)
                }
                // 否则为普通图
                else {
                    Tuple(width, height)
                }
            }
        }

}