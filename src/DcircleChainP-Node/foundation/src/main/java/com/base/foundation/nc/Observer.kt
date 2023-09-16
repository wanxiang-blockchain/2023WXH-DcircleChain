package com.base.foundation.nc

import java.lang.ref.WeakReference

class Observer(var num:Int, observer: ObserverAble?=null) {
	private val observerRef: WeakReference<ObserverAble> = WeakReference(observer)

	val observerAble:ObserverAble? get() {
		return observerRef.get()
	}
}