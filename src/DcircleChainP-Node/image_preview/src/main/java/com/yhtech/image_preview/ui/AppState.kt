package com.yhtech.image_preview.ui

import android.util.Log
import com.base.foundation.AppStateValue
import com.base.foundation.DCircleScope
import com.base.foundation.api.getAppState
import com.base.foundation.api.setAppState
import com.base.foundation.db.ChatCmd
import com.base.foundation.db.FindByChatId
import com.base.foundation.db.FindNotExecuteChatId
import com.base.foundation.getUs
import com.base.foundation.nc.ObserverAble
import com.yhtech.image_preview.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

/**
 * 1、进入 App，默认为 AppConnectedState
 */
abstract class AppState(var owner:Owner):ObserverAble {
	interface Owner {
		suspend fun SetStateTo(oldState:AppState, newState:AppState):Error?
		suspend fun setTitle(id:Int)
		suspend fun setTitleLoading(loading:Boolean)
		suspend fun setTitleFailed()
	}

	abstract suspend fun DidEnter()
	abstract suspend fun WillLeave()
	abstract fun value():AppStateValue
	protected suspend fun SetStateToNext(newState:AppState):Error? {
		Log.d(AppState::class.java.simpleName, "SetStateToNext oldState=${this::class.java.simpleName} newState=${newState::class.java.simpleName} start")

		this.WillLeave()
		val err = owner.SetStateTo(this, newState)
		if (err!=null) {
			Log.w(AppState::class.java.simpleName, "${this::class.java.simpleName} ${::SetStateToNext.name}  err=${err}")
			return err
		}
		Log.d(AppState::class.java.simpleName, "SetStateToNext oldState=${this::class.java.simpleName} newState=${newState::class.java.simpleName} end")

		newState.DidEnter()

		return null
	}
	open suspend fun sync() {}
	open suspend fun recover() {
		this.SetStateToNext(AppConnectingState(owner))
	}
}

class AppSyncingState(owner: Owner) : AppState(owner) {
	override suspend fun DidEnter() {
		owner.setTitle(R.string.message_home_list_top_state)
		owner.setTitleLoading(true)

		withContext(Dispatchers.IO) {
			setAppState(getAppState())
		}

		this.SetStateToNext(AppSyncedState(owner))
	}

	override suspend fun WillLeave() {
	}

	override fun value(): AppStateValue {
		return AppStateValue.Syncing
	}
}

class AppSyncedState(owner: Owner) : AppState(owner) {
	private val mutex = Mutex()
	override suspend fun DidEnter() {
		owner.setTitle(R.string.app_name_dcircle)
		owner.setTitleLoading(false)

		withContext(Dispatchers.Main) {
			getUs().nc.removeEvent(this@AppSyncedState, ChatCmd.NewEvent::class.java)
			getUs().nc.addObserver(this@AppSyncedState, ChatCmd.NewEvent::class.java) {
				DCircleScope.launch {
					mutex.lock()
					process()
					mutex.unlock()
				}
			}
		}

		mutex.lock()
		process()
		mutex.unlock()
	}

	private suspend fun process() {
		withContext(Dispatchers.IO) {
			val notExecute = ChatCmd.FindNotExecuteChatId()
			if (notExecute.isEmpty()) {
				return@withContext
			}
			notExecute.map {
				async { process(it) }
			}.awaitAll()
		}
	}
	private suspend fun process(chatId:String) {
		val docs = ChatCmd.FindByChatId(chatId)
		if (docs.isEmpty()) {
			return
		}

		for (doc in docs) {
			if (doc.data.isEmpty()) {
				continue
			}

		}
//		docs.delete()

		process(chatId)
	}

	override suspend fun WillLeave() {
		getUs().nc.removeAll(this)
	}

	override suspend fun sync() {
		this.SetStateToNext(AppSyncingState(owner))
	}

	override fun value(): AppStateValue {
		return AppStateValue.Synced
	}
}

class AppConnectingState(owner: Owner) : AppState(owner) {
	override suspend fun DidEnter() {
		owner.setTitle(R.string.message_waiting_network)
		owner.setTitleLoading(true)

		for (i in 0 until 10) {
			val err = setAppState(getAppState())
			if (err==null) {
				SetStateToNext(AppSyncingState(owner))
				return
			}

			delay(3000)
		}

		SetStateToNext(AppDisconnectedState(owner))
	}

	override suspend fun WillLeave() {
	}

	override suspend fun recover() {
	}

	override fun value(): AppStateValue {
		return AppStateValue.Connecting
	}
}

class AppDisconnectedState(owner: Owner) : AppState(owner) {
	override suspend fun DidEnter() {
		owner.setTitle(R.string.message_home_list_top_state_disconnected)
		owner.setTitleFailed()
	}

	override suspend fun WillLeave() {
	}

	override fun value(): AppStateValue {
		return AppStateValue.Disconnected
	}

	override suspend fun sync() {
		this.SetStateToNext(AppConnectingState(owner))
	}
}