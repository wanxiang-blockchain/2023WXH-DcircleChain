package com.base.thridpart

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.view.View
import android.widget.TextView
import com.blankj.utilcode.util.GsonUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.math.floor


fun View.setOnClickDelay(onClick: (View) -> Unit) {
    val debounceTime: Long = 500
    var lastClickTime = 0L

    this.setOnClickListener { view ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceTime) {
            lastClickTime = currentTime
            onClick(view)
        }
    }
}

fun View.setOnClickDelay(onClick: suspend (View) -> Unit, debounceTime: Long = 500) {
    var lastClickTime = 0L

    this.setOnClickListener { view ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceTime) {
            lastClickTime = currentTime
            CoroutineScope(Dispatchers.Main).launch {
                onClick(view)
            }
        }
    }
}

fun View.setInVisible(boolean: Boolean) {
    this.visibility = if (!boolean) View.VISIBLE else View.INVISIBLE
}

fun View.setVisible(boolean: Boolean) {
    this.visibility = if (boolean) View.VISIBLE else View.GONE
}

fun Any.toJson(): String {
    return GsonUtils.toJson(this)
}

fun ByteArray.toHexString(): String {
    var result = ""
    for (i in this.indices) {
        var hexString = Integer.toHexString(this[i].toInt() and 0xFF)
        if (hexString.length == 1) {
            hexString = "0$hexString"
        }
        result += hexString
    }
    return result
}

fun TextView.setUnderLine() {
    paint.flags = Paint.UNDERLINE_TEXT_FLAG
    paint.isAntiAlias = true
}

fun String.sha256(): ByteArray {
    var messageDigest: MessageDigest? = null
    try {
        messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(this.encodeToByteArray())
        return messageDigest.digest()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
    }
    return ByteArray(0)
}

fun getBitmapByBytes(compress: ByteArray): Bitmap {
    val imgByte = compress
    var input: InputStream? = null
    var bitmap: Bitmap? = null
    val options = BitmapFactory.Options()
    options.inSampleSize = 1
    input = ByteArrayInputStream(imgByte)
    /*   val softRef: SoftReference<*> = SoftReference<Any?>(
           BitmapFactory.decodeStream(
               input, null, options
           )
       ) //软引用防止OOM*/
    bitmap = BitmapFactory.decodeStream(
        input, null, options
    )
    try {
        if (input != null) {
            input.close()
        }
    } catch (e: IOException) {
        // 异常捕获
        e.printStackTrace()
    }
    return bitmap!!
}
