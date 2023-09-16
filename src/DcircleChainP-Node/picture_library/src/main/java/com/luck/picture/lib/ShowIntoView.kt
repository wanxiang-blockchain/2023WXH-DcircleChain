package com.luck.picture.lib

import android.util.Log
import androidx.core.net.toUri
import com.base.foundation.Aes
import com.base.foundation.DCircleScope
import com.base.foundation.db.DownloadTask
import com.base.foundation.getUs
import com.base.foundation.oss.DecryptOSSToDBFile
import com.base.foundation.oss.GetSandboxDBFile
import com.base.foundation.oss.GetSandboxOSSFile
import com.base.foundation.oss.Priority
import com.luck.picture.lib.widget.longimage.ImageSource
import com.luck.picture.lib.widget.longimage.SubsamplingScaleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

open class ShowIntoViewListener {
	open fun onImageLoad(view: SubsamplingScaleImageView) {}
	open suspend fun onImageLoadError(err:Error) {}
	open fun config(view:SubsamplingScaleImageView) {
		view.setOnTouchListener { _, event ->
			true
		}
		view.isZoomEnabled = false
		view.isPanEnabled = false
		view.setDoubleTapZoomScale(1.0f)
	}
	open fun onAddDownloadTask() {}
	open fun onlyDB():Boolean {
		return false
	}
}

private var events:MutableMap<SubsamplingScaleImageView,  DownloadTask.SuccessEvent> = mutableMapOf()
suspend fun ShowIntoView(objectId: String, key:String, view: SubsamplingScaleImageView, priority: Priority = Priority.Channel4,
												 listener:ShowIntoViewListener=ShowIntoViewListener()) {
	if (objectId.isEmpty() || key.isEmpty()) {
		return
	}

	val (decryptedFile, err) = GetSandboxDBFile().read(objectId)
	if (err == null) {
		withContext(Dispatchers.Main) {
			ShowIntoView(decryptedFile, view, listener)
		}
		return
	}

	if (GetSandboxOSSFile().has(objectId)) {
		DecryptOSSToDBFile(objectId, Aes(key))?.apply {
			return
		}

		return ShowIntoView(objectId, key,view, priority, listener)
	}

	if (listener.onlyDB()) {
		listener.onImageLoadError(Error("not found"))
		return
	}

	events[view]?.apply {
		getUs().nc.removeEvent(this)
	}

	val event = DownloadTask.SuccessEvent(objectId)
	events[view] = event

	getUs().nc.addEvent(event) { _, removeIt ->
		removeIt()

		events[view]?.apply {
			events.remove(view)
			DCircleScope.launch {
				ShowIntoView(objectId, key, view, priority, listener)
			}
		}
	}
DCircleScope.launch {
	listener.onAddDownloadTask()
}
	val task = DownloadTask(objectId, 0)
	task.Priority = priority.value
	getUs().downloader.AddTask(task)
}

fun ShowIntoView(file: File, view: SubsamplingScaleImageView, listener:ShowIntoViewListener = ShowIntoViewListener()) {
	listener.config(view)
	view.setImage(ImageSource.uri(file.toUri()))
	view.setOnImageEventListener(object : SubsamplingScaleImageView.OnImageEventListener {
		override fun onReady() {
			Log.d("ShowIntoView", "onReady file=${file}")
		}

		override fun onImageLoaded() {
			Log.d("ShowIntoView", "onImageLoaded file=${file}")
			listener.onImageLoad(view)
		}

		override fun onPreviewLoadError(e: Exception?) {
			Log.d("ShowIntoView", "onPreviewLoadError err=${e} file=${file}")
		}

		override fun onImageLoadError(e: Exception?) {
			Log.d("ShowIntoView", "onImageLoadError err=${e} file=${file}")
			DCircleScope.launch {
				listener.onImageLoadError(Error(e.toString()))
			}
		}

		override fun onTileLoadError(e: Exception?) {
			Log.d("ShowIntoView", "onTileLoadError err=${e} file=${file}")
		}

		override fun onPreviewReleased() {
			Log.d("ShowIntoView", "onPreviewReleased file=${file}")
		}
	})
}