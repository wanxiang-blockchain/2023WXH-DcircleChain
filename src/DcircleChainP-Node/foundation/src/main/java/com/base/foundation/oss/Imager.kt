package com.base.foundation.oss

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import com.base.foundation.ImageHandle
import com.base.thridpart.getBitmapByBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.min

class FileSize {
   var width:Int =0
   var height:Int =0
}

interface Imager {
   suspend fun thumb(data:File): File
    suspend  fun large(data:File):File
    fun size(data:ByteArray):FileSize

    fun size(file: File):FileSize

    suspend fun thumb(file: File, w: Int, h: Int):File
}

enum class ImageType{
    thumb,
    large,
}

class BrowserImager: Imager {
    private var imageHandle = ImageHandle()
    override suspend fun thumb(data: File): File {
        return thumb(data, 100, 300)
    }

  private fun saveToFile(file: File, bitmap: Bitmap): Boolean {
    return try {
      val outputStream: OutputStream = FileOutputStream(file)
      bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
      outputStream.close()
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  private fun thumbnail(file: File, maxWidth: Int, maxHeight: Int): Bitmap? {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(file.absolutePath, options)

    val width = options.outWidth
    val height = options.outHeight

    val aspectRatio = width.toFloat() / height.toFloat()

    var newWidth = maxWidth
    var newHeight = (newWidth / aspectRatio).toInt()

    if (newHeight > maxHeight) {
      newHeight = maxHeight
      newWidth = (newHeight * aspectRatio).toInt()
    }

    options.inJustDecodeBounds = false
    val originalBitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null

    // Scale the bitmap to fit within maxWidth and maxHeight while maintaining aspect ratio
    val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false)

    // Calculate the cropping position to center the scaled bitmap
    val x = (scaledBitmap.width - maxWidth) / 2
    val y = (scaledBitmap.height - maxHeight) / 2

    // Ensure y is not negative
    val actualY = maxOf(0, y)

    // Make sure the cropping area is within the scaled bitmap's boundaries
    val actualWidth = min(maxWidth, scaledBitmap.width - x)
    val actualHeight = min(maxHeight, scaledBitmap.height - actualY)

    // Create a matrix to apply the cropping
    val matrix = Matrix()
    matrix.postScale(1f, 1f)
    matrix.postTranslate(-x.toFloat(), -actualY.toFloat())

    // Apply the matrix to the cropped bitmap
    return Bitmap.createBitmap(scaledBitmap, x, actualY, actualWidth, actualHeight, matrix, true)
  }

  override suspend fun thumb(file: File, w:Int, h:Int): File {
    return  withContext(Dispatchers.Default) {
      val size = size(file)
      val objectId = file.name + "." + ImageType.thumb.name+".w${w}h${h}"
      val output = GetSandboxDBFile().path(objectId)
      if (GetSandboxDBFile().has(objectId)) {
        return@withContext File(output)
      }

      try {
        Log.d("getThumb", "from=${file.absolutePath} output=${output} x=${size.width} y=${size.height} w=${w} h=${h}")
        imageHandle.getThumb(file.absolutePath, output, size.width, size.height, w, h)
        if (File(output).exists()) {
          return@withContext File(output)
        }
      } catch (e:java.lang.Exception) {
        Log.w("getThumb", "from=${file.absolutePath} output=${output} err=$e")
      }

      try {
        thumbnail(file, w, h)?.apply {
          saveToFile(File(output), this)

          if (File(output).exists()) {
            return@withContext File(output)
          }
        }
      } catch (e:Exception) {
        e.printStackTrace()
      }

      return@withContext file
    }
  }

  override suspend fun large(data: File): File {
    val objectId = data.name + "." + ImageType.large.name
    val output = GetSandboxDBFile().path(objectId)
    if (GetSandboxDBFile().has(objectId)) {
      return File(output)
    }

    return withContext(Dispatchers.Default) {
          imageHandle.getLarge(data.absolutePath, output)
          return@withContext File(output)
      }
  }

  override fun size(data: ByteArray): FileSize {
      val bitmap = getBitmapByBytes(data)
      val fileSize = FileSize()
      fileSize.height = bitmap.height
      fileSize.width = bitmap.width
      return fileSize
  }

  override fun size(file: File): FileSize {
      val option = BitmapFactory.Options()
      option.inJustDecodeBounds = true
      val bitmap = BitmapFactory.decodeFile(file.absolutePath)
      val fileSize = FileSize()
      if (bitmap == null) {
          fileSize.height = 150
          fileSize.width = 100
          return fileSize
      }
      fileSize.height = bitmap.height
      fileSize.width = bitmap.width
      return fileSize
  }
}

fun getImager():Imager {
    return  BrowserImager()
}
