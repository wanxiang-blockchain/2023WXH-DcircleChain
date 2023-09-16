package com.base.foundation.nc

abstract class NcEvent<T>(var ids: List<T> = listOf()) {
	open fun getName():String {
		return this::class.java.name
	}
}

interface ObserverAble{
	fun getName() : String {
		return this::class.java.name
	}
}