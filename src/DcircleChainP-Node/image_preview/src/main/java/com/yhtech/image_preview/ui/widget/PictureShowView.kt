package com.yhtech.image_preview.ui.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.base.foundation.DCircleScope
import com.base.foundation.db.DownloadTask
import com.base.foundation.db.GetProgress
import com.base.foundation.getUs
import com.base.foundation.nc.ObserverAble
import com.base.thridpart.setVisible
import com.blankj.utilcode.util.SizeUtils
import com.google.gson.Gson
import com.luck.picture.lib.ShowIntoView
import com.luck.picture.lib.ShowIntoViewListener
import com.luck.picture.lib.tools.ScreenUtils
import com.luck.picture.lib.widget.longimage.SubsamplingScaleImageView
import com.yhtech.image_preview.databinding.PictureShowViewBinding
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

class AvatarImage {
    var objectId: String = ""
    var key: String = ""

    override fun equals(other: Any?): Boolean {
        return objectId==(other as AvatarImage).objectId && key== other.key
    }

    companion object
}

class PictureShowView(context: Context,attributeSet: AttributeSet):FrameLayout(context,attributeSet),ObserverAble {
    var origin:AvatarImage = AvatarImage()
    var large:AvatarImage = AvatarImage()
    private var thumb:AvatarImage = AvatarImage()

    val binding = PictureShowViewBinding.inflate(LayoutInflater.from(context),this,true)

    private suspend fun loadFromDb() {
        Log.d("PictureShowView", "loadFromDb origin=${Gson().toJson(origin)} thumb=${Gson().toJson(thumb)}")

        binding.circleProgressBar.setVisible(false)
        binding.imgPicThumb.setVisible(true)
        ShowIntoView(thumb.objectId, thumb.key, binding.imgPicThumb, listener = object : ShowIntoViewListener() {
            override fun config(view: SubsamplingScaleImageView) {
                var startX = 0f
                var startY = 0f
                view.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            startX = event.x
                            startY = event.y
                        }
                        MotionEvent.ACTION_UP -> {
                            val endX = event.x
                            val endY = event.y

                            val deltaX = abs(endX - startX)
                            val deltaY = abs(endY - startY)

                            val tapThreshold = ViewConfiguration.get(context).scaledTouchSlop

                            if (deltaX <= tapThreshold && deltaY <= tapThreshold) {
                                (context as AppCompatActivity).finishAfterTransition()
                                true
                            }
                        }
                    }

                    false
                }
            }
        })

        if ( binding.imgPicThumb.measuredHeight > ScreenUtils.getScreenHeight(context)){
            val lp = binding.imgPicThumb.layoutParams as MarginLayoutParams
            val topMargin = (ScreenUtils.getScreenHeight(context) - SizeUtils.dp2px(300f))/2
            lp.setMargins(0,topMargin,0,0)
            binding.imgPicThumb.layoutParams= lp
        }

        // 如果有原图，则优先加载本地原图，如果加载失败，则加载本地或者网络的大图
        if (origin.objectId.isNotEmpty()) {
            ShowIntoView(origin.objectId, origin.key, binding.imgPic, listener = object : ShowIntoViewListener() {
                override fun onlyDB(): Boolean {
                    return true
                }

                override fun onImageLoad(view: SubsamplingScaleImageView) {
                    binding.imgPicThumb.setVisible(false)
                    binding.imgPic.setVisible(true)

                    val viewWidth = view.width
                    val viewHeight = view.height
                    val imageWidth = view.sWidth
                    val imageHeight = view.sHeight

                    if (viewWidth > 0 && viewHeight > 0 && imageWidth > 0 && imageHeight > 0) {
                        val scale = min(viewWidth.toFloat() / imageWidth, viewHeight.toFloat() / imageHeight)
                        val centerX = (viewWidth - scale * imageWidth) / 2f
                        val centerY = (viewHeight - scale * imageHeight) / 2f

                        view.setScaleAndCenter(scale, PointF(centerX, centerY))
                    }
                }

                override fun config(view: SubsamplingScaleImageView) {
                    var startX = 0f
                    var startY = 0f
                    view.setOnTouchListener { _, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                startX = event.x
                                startY = event.y
                            }
                            MotionEvent.ACTION_UP -> {
                                val endX = event.x
                                val endY = event.y

                                val deltaX = abs(endX - startX)
                                val deltaY = abs(endY - startY)

                                val tapThreshold = ViewConfiguration.get(context).scaledTouchSlop

                                if (deltaX <= tapThreshold && deltaY <= tapThreshold) {
                                    (context as AppCompatActivity).finishAfterTransition()
                                    true
                                }
                            }
                        }

                        false
                    }
                }

                override suspend fun onImageLoadError(err: Error) {
                    getUs().nc.addObserver(this@PictureShowView, DownloadTask.SuccessEvent(origin.objectId)) {
                        getUs().nc.removeEvent(this@PictureShowView, DownloadTask.SuccessEvent(origin.objectId))
                        DCircleScope.launch {
                            ShowIntoView(origin.objectId, origin.key, binding.imgPicThumb, listener = object : ShowIntoViewListener() {
                                override fun onlyDB(): Boolean {
                                    return true
                                }

                                override fun onImageLoad(view: SubsamplingScaleImageView) {
                                    binding.imgPicThumb.setVisible(true)
                                    binding.imgPic.setVisible(false)

                                    val viewWidth = view.width
                                    val viewHeight = view.height
                                    val imageWidth = view.sWidth
                                    val imageHeight = view.sHeight

                                    if (viewWidth > 0 && viewHeight > 0 && imageWidth > 0 && imageHeight > 0) {
                                        val scale = min(
                                            viewWidth.toFloat() / imageWidth,
                                            viewHeight.toFloat() / imageHeight
                                        )
                                        val centerX = (viewWidth - scale * imageWidth) / 2f
                                        val centerY = (viewHeight - scale * imageHeight) / 2f

                                        view.setScaleAndCenter(scale, PointF(centerX, centerY))
                                    }
                                }

                                override fun config(view: SubsamplingScaleImageView) {
                                    var startX = 0f
                                    var startY = 0f
                                    view.setOnTouchListener { _, event ->
                                        when (event.action) {
                                            MotionEvent.ACTION_DOWN -> {
                                                startX = event.x
                                                startY = event.y
                                            }
                                            MotionEvent.ACTION_UP -> {
                                                val endX = event.x
                                                val endY = event.y

                                                val deltaX = abs(endX - startX)
                                                val deltaY = abs(endY - startY)

                                                val tapThreshold =
                                                    ViewConfiguration.get(context).scaledTouchSlop

                                                if (deltaX <= tapThreshold && deltaY <= tapThreshold) {
                                                    (context as AppCompatActivity).finishAfterTransition()
                                                    true
                                                }
                                            }
                                        }

                                        false
                                    }
                                }
                            })
                        }
                    }

                    ShowIntoView(large.objectId, large.key, binding.imgPic, listener = object : ShowIntoViewListener() {
                        override fun onAddDownloadTask() {
                            binding.circleProgressBar.setVisible(true)
                            val animator = ObjectAnimator.ofFloat(binding.circleProgressBar, "rotation", 0f, 360f)
                            animator.duration = 1000
                            animator.repeatCount = ObjectAnimator.INFINITE
                            animator.repeatMode = ObjectAnimator.RESTART
                            animator.start()
                            getUs().nc.addObserver(this@PictureShowView, DownloadTask.ProgressEvent(large.objectId)) {
                                DCircleScope.launch {
                                    val progress = DownloadTask.GetProgress(large.objectId)
                                    if (progress>=100) {
                                        binding.circleProgressBar.setVisible(false)
                                        getUs().nc.removeEvent(this@PictureShowView, DownloadTask.ProgressEvent(large.objectId))
                                        getUs().nc.removeEvent(this@PictureShowView, DownloadTask.SuccessEvent(large.objectId))
                                    }

                                }
                            }
                            getUs().nc.addObserver(this@PictureShowView, DownloadTask.SuccessEvent(large.objectId)) {
                                binding.circleProgressBar.setVisible(false)
                                getUs().nc.removeEvent(this@PictureShowView, DownloadTask.ProgressEvent(large.objectId))
                                getUs().nc.removeEvent(this@PictureShowView, DownloadTask.SuccessEvent(large.objectId))
                            }
                        }
                        override fun onImageLoad(view: SubsamplingScaleImageView) {
                            binding.imgPicThumb.setVisible(false)
                            binding.imgPic.setVisible(true)
                            binding.circleProgressBar.setVisible(false)
                            val viewWidth = view.width
                            val viewHeight = view.height
                            val imageWidth = view.sWidth
                            val imageHeight = view.sHeight

                            if (viewWidth > 0 && viewHeight > 0 && imageWidth > 0 && imageHeight > 0) {
                                val scale = min(viewWidth.toFloat() / imageWidth, viewHeight.toFloat() / imageHeight)
                                val centerX = (viewWidth - scale * imageWidth) / 2f
                                val centerY = (viewHeight - scale * imageHeight) / 2f

                                view.setScaleAndCenter(scale, PointF(centerX, centerY))
                            }
                        }

                        override fun config(view: SubsamplingScaleImageView) {
                            var startX = 0f
                            var startY = 0f
                            view.setOnTouchListener { _, event ->
                                when (event.action) {
                                    MotionEvent.ACTION_DOWN -> {
                                        startX = event.x
                                        startY = event.y
                                    }
                                    MotionEvent.ACTION_UP -> {
                                        val endX = event.x
                                        val endY = event.y

                                        val deltaX = abs(endX - startX)
                                        val deltaY = abs(endY - startY)

                                        val tapThreshold = ViewConfiguration.get(context).scaledTouchSlop

                                        if (deltaX <= tapThreshold && deltaY <= tapThreshold) {
                                            (context as AppCompatActivity).finishAfterTransition()
                                            true
                                        }
                                    }
                                }

                                false
                            }
                        }
                    })
                }
            })
            return
        }

        // 没有原图，则加载本地或者网络的大图
        ShowIntoView(large.objectId, large.key, binding.imgPic, listener = object : ShowIntoViewListener() {
            override fun onAddDownloadTask() {
                binding.circleProgressBar.setVisible(true)
                val animator = ObjectAnimator.ofFloat(binding.circleProgressBar, "rotation", 0f, 360f)
                animator.duration = 1000
                animator.repeatCount = ObjectAnimator.INFINITE
                animator.repeatMode = ObjectAnimator.RESTART
                animator.start()
                getUs().nc.addObserver(this@PictureShowView, DownloadTask.ProgressEvent(origin.objectId)) {
                    DCircleScope.launch {
                        val progress = DownloadTask.GetProgress(origin.objectId)
                        if (progress>=100) {
                            binding.circleProgressBar.setVisible(false)
                            getUs().nc.removeEvent(this@PictureShowView, DownloadTask.ProgressEvent(origin.objectId))
                            getUs().nc.removeEvent(this@PictureShowView, DownloadTask.SuccessEvent(origin.objectId))
                        }
                    }
                    binding.circleProgressBar.setVisible(false)
                }
                getUs().nc.addObserver(this@PictureShowView, DownloadTask.SuccessEvent(origin.objectId)) {
                    binding.circleProgressBar.setVisible(false)
                    getUs().nc.removeEvent(this@PictureShowView, DownloadTask.ProgressEvent(origin.objectId))
                    getUs().nc.removeEvent(this@PictureShowView, DownloadTask.SuccessEvent(origin.objectId))
                }
            }
            override fun onImageLoad(view: SubsamplingScaleImageView) {
                binding.imgPicThumb.setVisible(false)
                binding.imgPic.setVisible(true)
                binding.circleProgressBar.setVisible(false)
                val viewWidth = view.width
                val viewHeight = view.height
                val imageWidth = view.sWidth
                val imageHeight = view.sHeight

                if (viewWidth > 0 && viewHeight > 0 && imageWidth > 0 && imageHeight > 0) {
                    val scale = min(viewWidth.toFloat() / imageWidth, viewHeight.toFloat() / imageHeight)
                    val centerX = (viewWidth - scale * imageWidth) / 2f
                    val centerY = (viewHeight - scale * imageHeight) / 2f

                    view.setScaleAndCenter(scale, PointF(centerX, centerY))
                }
            }

            override fun config(view: SubsamplingScaleImageView) {
                var startX = 0f
                var startY = 0f
                view.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            startX = event.x
                            startY = event.y
                        }
                        MotionEvent.ACTION_UP -> {
                            val endX = event.x
                            val endY = event.y

                            val deltaX = abs(endX - startX)
                            val deltaY = abs(endY - startY)

                            val tapThreshold = ViewConfiguration.get(context).scaledTouchSlop

                            if (deltaX <= tapThreshold && deltaY <= tapThreshold) {
                                (context as AppCompatActivity).finishAfterTransition()
                                true
                            }
                        }
                    }

                    false
                }
            }
        })
    }

    fun SetImage(origin:AvatarImage, large:AvatarImage, thumb:AvatarImage) {
        this.origin = origin
        this.thumb = thumb
        this.large = large
        DCircleScope.launch {
            loadFromDb()
        }
    }

    fun setWidthHeight(width: Int,height: Int) {
        val layoutParams = LayoutParams(width,height)
        binding.root.layoutParams = layoutParams
    }
}