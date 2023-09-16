package com.base.baseui.widget.others

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.InputType
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.base.baseui.R

class PasswordEditText(context: Context,attrs:AttributeSet): AppCompatEditText(context,attrs) {

    private var visibleIcon: Drawable? = null
    private var invisibleIcon: Drawable? = null
    private var isPasswordVisible = false

    init {
        visibleIcon = ContextCompat.getDrawable(context, R.mipmap.ic_eye_open)
        invisibleIcon = ContextCompat.getDrawable(context, R.mipmap.ic_eye_closed)
        setupIcons()
    }

    private fun setupIcons() {
        visibleIcon?.setBounds(0, 0, visibleIcon!!.intrinsicWidth, visibleIcon!!.intrinsicHeight)
        invisibleIcon?.setBounds(0, 0, invisibleIcon!!.intrinsicWidth, invisibleIcon!!.intrinsicHeight)
        setCompoundDrawables(null, null, if (isPasswordVisible) visibleIcon else invisibleIcon, null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val touchable = event.x > width - paddingRight - visibleIcon!!.intrinsicWidth
            if (touchable) {
                togglePasswordVisibility()
                event.action = MotionEvent.ACTION_CANCEL
            }
        }
        return super.onTouchEvent(event)
    }

    private fun togglePasswordVisibility() {
        inputType = if (isPasswordVisible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        isPasswordVisible = !isPasswordVisible
        setupIcons()
    }

}