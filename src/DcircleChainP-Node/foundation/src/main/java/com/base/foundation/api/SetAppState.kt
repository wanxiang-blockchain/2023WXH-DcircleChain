package com.base.foundation.api
import com.base.foundation.sendSus


class SetAppStateResponse

class SetAppStateRequest{
	var state:Int = 0
}

enum class AppState(var value:Int) {
	None(0),
	Background(1),
	Foreground(2),
}

var appState_ = AppState.None

fun getAppState():AppState {
	return appState_
}

suspend fun setAppState(appState:AppState, onlyDB:Boolean = false): Error? {
	appState_ = appState
    if (onlyDB) {
		return null
	}

	val request = SetAppStateRequest()
	request.state = appState.value
	val (_, err) = sendSus("/im/chat/SetAppState", request, SetAppStateResponse::class.java)

	return err
}
