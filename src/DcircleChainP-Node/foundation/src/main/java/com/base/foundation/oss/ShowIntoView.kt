package com.base.foundation.oss

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.LruCache
import android.widget.ImageView
import com.base.foundation.Aes
import com.base.foundation.DCircleScope
import com.base.foundation.db.DownloadTask
import com.base.foundation.getAppContext
import com.base.foundation.getUs
import com.base.foundation.nc.ObserverAble
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

open class OnMediaStateChangeListener {
	open fun onLoad(file: File){
	}
	open fun onLoad(file: File, size:ImageSize?) {
	}
	open fun onLoad(resource: Drawable) {
	}
	open fun onFail(err:Error){
	}
	open fun config(glide: RequestBuilder<Drawable>):RequestBuilder<Drawable> {
		return glide
	}
	open fun glideCacheKey():String {
		return ""
	}
}

val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
val cacheSize = maxMemory / 16
val cache: LruCache<String, WeakReference<BitmapDrawable>> = LruCache(cacheSize)
suspend fun ShowIntoView(objectId: String, key:String, view: ImageView, priority: Priority = Priority.Channel4,
												 listener:OnMediaStateChangeListener=OnMediaStateChangeListener(), blur:Boolean=false):Error? {
	return withContext(Dispatchers.Default) {
		if (objectId.isEmpty() || key.isEmpty()) {
			return@withContext Error("objectId or key is empty.")
		}

		if (view !is ObserverAble) {
			throw Error("View(${view.javaClass::class.java.name}) not impl ObserverAble interface")
		}

		var (decryptedFile, err) = GetSandboxDBFile().read(objectId)
		if (err == null) {
			val imageSize = getImageSize(decryptedFile)
			imageSize?.apply {
				// FIX: 头像分辨率最高是 640，这儿主要处理 DID 图片数据
				if (this.width > view.width && this.height > view.height && this.width > 640 && this.height > 640 && view.width>0 && view.height>0) {
					Log.w("ShowIntoView", "file=${decryptedFile} view's width=${view.width} height=${view.height} imageSize=${Gson().toJson(imageSize)}")
					decryptedFile = getImager().thumb(decryptedFile, view.width, view.height)
				}
			}

			withContext(Dispatchers.Main) {
				ShowIntoView(decryptedFile, view, listener, blur)
			}
			return@withContext null
		}

		if (GetSandboxOSSFile().has(objectId)) {
			DecryptOSSToDBFile(objectId, Aes(key))?.apply {
				return@withContext this
			}

			return@withContext ShowIntoView(objectId, key,view, priority, listener, blur)
		}

		if (!getUs().isValid()) {
			return@withContext Error("account not login")
		}

		Log.d(OSSNetDownloader::class.java.simpleName, "ShowIntoView downloader AddTask objectId=${objectId} key=${key}")
		return@withContext withContext(Dispatchers.Main) {
			val event = DownloadTask.SuccessEvent(objectId)
			getUs().nc.addObserver(view as ObserverAble, event) {
				getUs().nc.removeEvent(view as ObserverAble, event)
				DCircleScope.launch {
					ShowIntoView(objectId, key, view, priority, listener, blur)
				}
			}

			val task = DownloadTask(objectId, 0)
			task.Priority = priority.value
			return@withContext getUs().downloader.AddTask(task)
		}
	}
}

data class ImageSize(var width:Int, var height:Int)

fun getImageSize(file:File):ImageSize? {
	val option = BitmapFactory.Options()
	option.inJustDecodeBounds = true
	val bitmap = BitmapFactory.decodeFile(file.absolutePath)
	bitmap?.apply {
		return ImageSize(bitmap.width, bitmap.height)
	}

	return null
}

fun ShowIntoView(file:File, view: ImageView, listener:OnMediaStateChangeListener=OnMediaStateChangeListener(), blur:Boolean=false) {
	val imageSize = getImageSize(file)
	Log.d("ShowIntoView", "file=${file} size=${file.length()} bytes view's width=${view.width} height=${view.height} imageSize=${Gson().toJson(imageSize)}")
	var cacheKey = "${file}_${blur}"
	listener.glideCacheKey().let { subKey  ->
		if (subKey.isNotEmpty()) {
			cacheKey = "${cacheKey}_${subKey}"
		}
	}

	val resource = cache.get(cacheKey)?.get()
	for (i in 1..1) {
		if (resource==null) {
			break
		}

		val bitmap = resource.bitmap
		if (bitmap != null && bitmap.isRecycled) {
			cache.remove(cacheKey)
			break
		}

		view.setImageDrawable(resource)
		listener.onLoad(resource)
		listener.onLoad(file)
		listener.onLoad(file, imageSize)
		return
	}

	val start = Date().time
	var glide = Glide.with(getAppContext()).load(file)
		.addListener(object : RequestListener<Drawable> {
			override fun onLoadFailed(
				e: GlideException?,
				model: Any?,
				target: Target<Drawable>?,
				isFirstResource: Boolean
			): Boolean {
				Log.d("ShowIntoView", "onLoadFailed file=${file} ${e?.localizedMessage}")
				listener.onFail(Error("load fail (${e.toString()})"))
				return false
			}

			override fun onResourceReady(
				resource: Drawable?,
				model: Any?,
				target: Target<Drawable>?,
				dataSource: DataSource?,
				isFirstResource: Boolean
			): Boolean {
				if (resource is BitmapDrawable) {
					Log.d("ShowIntoView", "onResourceReady file=${file} imageSize=${Gson().toJson(imageSize)} bitmap's width=${resource.bitmap.width} height=${resource.bitmap.height} time=${Date().time - start}")
					cache.put(cacheKey, WeakReference(resource))
					listener.onLoad(resource)
				}
				listener.onLoad(file)
				listener.onLoad(file, imageSize)
				return false
			}
		})
		.dontAnimate()
		.skipMemoryCache(false)
		.diskCacheStrategy(DiskCacheStrategy.NONE)
		glide = listener.config(glide)
		if (blur) {
			glide = glide.apply(RequestOptions.bitmapTransform(BlurTransformation()))
		}
		glide.into(view)
}