package com.base.foundation

import com.base.foundation.nc.NcEvent

enum class AppStateValue (var int: Int) {
	None(0),
	Connecting(2),
	Syncing(3),
	Synced(4),
	Disconnected(5),
}

class AppStateValueChangeEvent(var value:AppStateValue = AppStateValue.None) : NcEvent<String>() {
	override fun getName(): String {
		return super.getName() + value
	}
}