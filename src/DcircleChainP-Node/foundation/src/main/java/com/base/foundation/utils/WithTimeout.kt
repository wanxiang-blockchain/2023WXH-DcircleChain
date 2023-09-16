package com.base.foundation.utils

import android.util.Log
import com.anywithyou.stream.Duration
import com.base.foundation.DCircleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.*

suspend fun <R>WithTimeout(delay:Duration, block:suspend ()-> R): R? {
	val ch:Channel<R?> = Channel(1)
	val timer = Timer()
	timer.schedule(object : TimerTask() {
		override fun run() {
			DCircleScope.launch {
				try {
					ch.send(null)
				} catch (_:Exception) {}
			}
		}

	}, delay.milliSecond())

	DCircleScope.launch {
		try {
			ch.send(block())
		} catch (e:CancellationException) {
			Log.w("Preload", "WithTimeout CancellationException $e")
		} catch (_:Exception) {}
	}


	return ch.receive()
}

suspend fun <R>WithTimeout(timeMillis:Long, block:suspend ()-> R): R? {
	return WithTimeout(Duration(timeMillis), block)
}