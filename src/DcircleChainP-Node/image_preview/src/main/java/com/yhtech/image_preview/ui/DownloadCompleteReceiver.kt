package com.yhtech.image_preview.ui

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.base.foundation.DCircleScope
import kotlinx.coroutines.launch

class DownloadCompleteReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		val action = intent.action
		if (DownloadManager.ACTION_DOWNLOAD_COMPLETE != action) {
			return
		}

		val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
		if (downloadId == -1L) {
			return
		}

		DCircleScope.launch {
		}
	}
}
