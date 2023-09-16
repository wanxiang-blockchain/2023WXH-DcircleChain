package com.yhtech.image_preview.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min
import kotlin.math.sqrt

class OssHexagonImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AppCompatImageView(context, attrs) {

    companion object {
        private val BITMAP_CONFIG = Bitmap.Config.ARGB_8888
    }

    private val _path = Path()
    private val _rect = Rect()
    private val _paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var _shader: BitmapShader? = null

    private var _bitmap: Bitmap? = null
    private var _width = 0
    private var _height = 0
    private var _bitmapWidth = 0
    private var _bitmapHeight = 0

    private val _shaderMatrix = Matrix()

    init {
//        _paint.style = Paint.Style.FILL
//        _paint.strokeWidth = 3f
    }


    private fun hexagonPath() {
        val d: Float = _width / 4 * (2 - sqrt(3F)) //六边形到边到内切圆的距离
        val r: Float = (_width / 4).toFloat()
        val p0x: Float = r
        val p0y: Float = d
        val p1x = r * 3
        val p2x = _width.toFloat()
        val p2y = (_width / 2).toFloat()
        val p3y = _width - d
        val p5x = 0F

        with(_path) {
            reset()
            moveTo(p0x, p0y)
            lineTo(p1x, d)
            lineTo(p2x, p2y)
            lineTo(p2x, p2y)
            lineTo(p1x, p3y)
            lineTo(p0x, p3y)
            lineTo(p5x, p2y)
            lineTo(p0x, p0y)
//            close()
        }
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        invalidate()
    }


    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        invalidate()
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        invalidate()
    }


    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
        _bitmap = getBitmapFromDrawable(drawable)
        setUp()
        hexagonPath()
        canvas.drawPath(_path, _paint)
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) return drawable.bitmap
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, BITMAP_CONFIG)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, _width, _height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun setUp() {
        val bitmap = _bitmap
        if (bitmap != null) {
            _shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            _bitmapWidth = bitmap.width
            _bitmapHeight = bitmap.height
//            updateShaderMatrix()
            _paint.shader = _shader
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                checkIfTouchOnEffectiveArea(event)
            }
        }
        return super.onTouchEvent(event)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (!checkIfTouchOnEffectiveArea(event)) return false
        return super.dispatchTouchEvent(event)
    }

    private fun checkIfTouchOnEffectiveArea(event: MotionEvent): Boolean {
        return computeRegion(_path).contains(event.x.toInt(), event.y.toInt())
    }

    private fun computeRegion(path: Path): Region {
        val region = Region()
        val f = RectF()
        path.computeBounds(f, true)
        region.setPath(path, Region(f.left.toInt(), f.top.toInt(), f.right.toInt(), f.bottom.toInt()))
        return region
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        _width = min(measuredWidth, measuredHeight)
        _height = _width
        setMeasuredDimension(_width, _width)
    }

}