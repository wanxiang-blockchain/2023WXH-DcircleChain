package com.github.xpwu.ktdbtable

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

var debugTag = "DBQueue"

private class dbCtx<T>(
  val db: DB<T>
) : AbstractCoroutineContextElement(dbCtx) {
  companion object Key : CoroutineContext.Key<dbCtx<*>>
}

class DBQueue<T>(logName: String = "db",
                 tablesBinding: List<TableBinding> = emptyList(),
                 upgrade: Boolean = true,
                 init: suspend ()->DBer<T>){

  private val scope = CoroutineScope(CoroutineName("DBQueue-$logName"))

  internal val queue: Channel<suspend (DB<T>)->Unit> = Channel(UNLIMITED)

  init {
    // consumer
    scope.launch {
      val db = DB(init(), tablesBinding, upgrade)

      withContext(dbCtx(db)) {
        while (isActive) {
          val exe = queue.receive()
          exe(db)
        }
      }
    }
  }

  fun Close() {
    scope.cancel()
  }
}

suspend operator fun <R, T> DBQueue<T>.invoke(block: suspend (DB<T>)->R): R {
  // process nest 
  val dbctx = currentCoroutineContext()[dbCtx.Key]
  if (dbctx != null) {
    Log.d(debugTag, "nest is ok")
    @Suppress("UNCHECKED_CAST")
    return block(dbctx.db as DB<T>)
  }

  val ch = Channel<R>(1)
  queue.send {
    ch.send(block(it))
  }

  return ch.receive()
}

suspend fun <R, T> DBQueue<T>.en(block: suspend (DB<T>)->R): R {
  return this(block)
}
