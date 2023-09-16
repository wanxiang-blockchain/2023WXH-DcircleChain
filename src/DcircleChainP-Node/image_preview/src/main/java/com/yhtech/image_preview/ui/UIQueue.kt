package com.yhtech.image_preview.ui

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

var debugTag = "UIQueue"

private class dbCtx : AbstractCoroutineContextElement(dbCtx) {
	companion object Key : CoroutineContext.Key<dbCtx>
}

class UIQueue(var context: CoroutineContext = Dispatchers.Main, var name:String = "default"){
	private val scope = CoroutineScope(context)

	internal val queue: Channel<suspend () -> Unit> = Channel(Channel.UNLIMITED)

	init {
		// consumer
		scope.launch {
			Log.d("UIQueue", "name=${name} start")
			withContext(dbCtx()) {
				while (isActive) {
					val exe = queue.receive()
					Log.d("UIQueue", "name=${name} exe start")
					exe()
					Log.d("UIQueue", "name=${name} exe end")
				}
			}
			Log.d("UIQueue", "name=${name} end")
		}
	}

	fun Close() {
		scope.cancel()
	}
}

suspend operator fun <R> UIQueue.invoke(block: suspend ()->R): R {
	// process nest 
	val dbctx = currentCoroutineContext()[dbCtx.Key]
	if (dbctx != null) {
		Log.d(debugTag, "nest is ok")
		@Suppress("UNCHECKED_CAST")
		return block()
	}

	val ch = Channel<R>(1)
	queue.send {
		ch.send(block())
	}

	return ch.receive()
}

suspend fun <R> UIQueue.en(block: suspend ()->R): R {
	return this(block)
}
