package com.base.foundation.nc

import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance


@Suppress("UNCHECKED_CAST")
 class NC {
	private var num: AtomicInteger = AtomicInteger(0)
	private var observers = ConcurrentHashMap<String,MutableList<Observer>>()
	private var clbs = ConcurrentHashMap<Int,(e:Any)->Unit>()
	private var events = ConcurrentHashMap<String,MutableList<Observer>>()

	companion object {
		const val TAG = "NotificationCenter"
	}
	private fun getCallerNo():String? {
		for (stackTrace in Throwable().stackTrace) {
			if (stackTrace.className == "com.base.foundation.nc.NC") {
				continue
			}

			return stackTrace.toString()
		}

		return null
	}


	fun <T, E: NcEvent<T>>addEvent(event:E, clb:(e:E, removeIt:()->Unit)->Unit) {
		val callerNo = getCallerNo()
		if (Looper.getMainLooper() != Looper.myLooper()) {
			throw RuntimeException("nc.addEvent must be called in mainThread")
		}

		val n = this.num.incrementAndGet()

		clbs[n] = {e->
			clb(e as E) {
				clbs.remove(n)
			}
		}

		var observerAble: ObserverAble?=null
		callerNo?.let {
			observerAble = object : ObserverAble {
				override fun getName(): String {
					return it
				}
			}
		}
		val old = events[event.getName()]?: mutableListOf()
		for (observer in old) {
			Log.w(TAG, "$callerNo addEvent ${event.getName()} duplicated, may be not remove, please check you logic.")
		}
		old.add(Observer(n, observerAble))
		events[event.getName()] = old
	}

	fun <T, E: NcEvent<T>>addEvent(event:KClass<E>, clb:(e:E, removeIt:()->Unit)->Unit){
		addEvent(event.createInstance(), clb)
	}

	fun <T, E: NcEvent<T>>addEvent(event:Class<E>, clb:(e:E, removeIt:()->Unit)->Unit){
		addEvent(event.newInstance(), clb)
	}

	fun <T, E : NcEvent<T>> removeEvent(observer: ObserverAble, event: Class<E>) {
		removeEvent(observer, event.newInstance())
	}

	fun <T, E : NcEvent<T>> removeEvent(observer: ObserverAble, event: KClass<E>) {
		removeEvent(observer, event.createInstance())
	}

	fun <T, E : NcEvent<T>> removeEvent(observer: ObserverAble, events: MutableList<E>) {
		for (event in events) {
			removeEvent(observer, event)
		}
	}
	fun <T, E : NcEvent<T>> removeEvent(observer: ObserverAble, event: E) {
		if (Looper.getMainLooper() != Looper.myLooper()) {
			throw RuntimeException("nc.removeEvent must be called in mainThread")
		}

		val es = events[event.getName()]
		val del:MutableList<Observer> = mutableListOf()
		es?.apply {
			for (ee in this) {
				if (ee.observerAble != observer) {
					continue
				}

				clbs.remove(ee.num)
				del.add(ee)
			}
		}
		events[event.getName()]?.removeAll(del)
	}

	fun <T, E: NcEvent<T>>addObserver(observer: ObserverAble, events:MutableList<E>, clb: (e:E)->Unit) {
		for (event in events) {
			addObserver(observer, event, clb)
		}
	}

	fun <T, E: NcEvent<T>>addObserver(observer: ObserverAble, event:E, clb: (e:E)->Unit) {
		if (Looper.getMainLooper() != Looper.myLooper()) {
			throw RuntimeException("nc.addObserver must be called in mainThread")
		}

		val n = this.num.incrementAndGet()

		clbs[n] = {
			clb(it as E)
		}

		val old = events[event.getName()] ?: mutableListOf()
		old.add(Observer(n, observer))
		events[event.getName()] = old.toMutableList()

        val old0 = observers[observer.getName()] ?: mutableListOf()
		observers[observer.getName()] = old0
	}

	fun <T, E: NcEvent<T>>addObserver(observer: ObserverAble, event:Class<E>, clb: (e:E)->Unit) {
		addObserver(observer, event.newInstance(), clb)
	}

	fun <T, E: NcEvent<T>>addObserver(observer: ObserverAble, event:KClass<E>, clb: (e:E)->Unit) {
		addObserver(observer, event.createInstance(), clb)
	}

	fun removeAll(observer: ObserverAble) {
		if (Looper.getMainLooper() != Looper.myLooper()) {
			throw RuntimeException("nc.removeAll must be called in mainThread")
		}

		val os = observers[observer.getName()]
		os?.apply {
			for (o in os){
				clbs.remove(o.num)
			}
		}

		val del:MutableList<Observer> = mutableListOf()
		for ((key, event) in events) {
			for (ee in event) {
				if (ee.observerAble != observer) {
					continue
				}

				clbs.remove(ee.num)
				del.add(ee)
			}

			event.removeAll(del)
			events[key] = event
		}
	}

	fun <T, E : NcEvent<T>> removeEvent(event: Class<E>) {
		removeEvent(event.newInstance())
	}

	fun <T, E : NcEvent<T>> removeEvent(event: KClass<E>) {
		removeEvent(event.createInstance())
	}

	fun <T, E : NcEvent<T>> removeEvent(event: E) {
		if (Looper.getMainLooper() != Looper.myLooper()) {
			throw java.lang.RuntimeException("nc.removeEvent must be called in mainThread")
		}

		val es = events[event.getName()]

		es?.apply {
			var er: Observer? = null
			for (ee in this) {
				if (event.getName() == ee.observerAble?.getName()) {
					er = ee
				}
				er?.apply {
					clbs.remove(er.num)
				}
			}
			events[event.getName()]?.removeAll(es)
		}
	}

	suspend fun <T, E: NcEvent<T>>postToMain(event:E) {
		withContext(Dispatchers.Main) {
			post(Gson().fromJson(Gson().toJson(event), event::class.java))
		}
	}

	private suspend fun <T, E: NcEvent<T>>post(event:E) {
		if (Looper.getMainLooper() != Looper.myLooper()) {
			throw RuntimeException("nc.post must be called in mainThread")
		}

		val delIndex = mutableMapOf<Int,Boolean>()
		try {
			event::class.java.newInstance()
        } catch (e:java.lang.Exception){
			throw RuntimeException("${event::class.java} Not Event")
		}
		val es = CopyOnWriteArrayList(events[event.getName()] ?: mutableListOf())
		val all = mutableListOf<Deferred<Unit>>()
		coroutineScope {
			for (i in es){
				val ef = clbs[i.num]
				if (ef == null){
					delIndex[i.num] = true
				}
				ef?.apply {
					all.add(async {
						ef(event)
					})
				}
			}
			all.awaitAll()
		}

		if (delIndex.size<=es.size/3 || es.size ==0){
			return
		}

		val newEs = mutableListOf<Observer>()
		for (i in 0 until es.size){
			if (delIndex[i] == true){
				newEs.add(es[i])
			}
		}
		events[event.getName()] = newEs
	}
}
