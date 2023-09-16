package com.yhtech.did.ui
import android.content.Context
import android.util.Log
import com.base.foundation.chain.SignResult
import com.base.foundation.db.*
import com.base.foundation.getUs


interface DIDArticleStateOwner {
	suspend fun SetStateTo(address:String, oldState:DIDArticleState, newState:DIDArticleState, onlyDB: Boolean):Error?
}

abstract class DIDArticleState {
	lateinit var address:String
	lateinit var context: Context
	lateinit var owner: DIDArticleStateOwner

	open suspend fun Upload():Error? {
		return Error("Prohibit")
	}

	open suspend fun Confirm(signResults: MutableList<SignResult>):Error? {
		return Error("Prohibit")
	}

	private fun msg(msg:String):String {
		return "address=${address} state=${this::class.java.simpleName} ${msg}"
	}

	open suspend fun EditAbstract():Error? {
		return Error("Prohibit")
	}

	open suspend fun WillLeave() {
		Log.d(DIDArticleState::class.java.simpleName, msg(::WillLeave.name))
	}
	open suspend fun DidEnter() {
		Log.d(DIDArticleState::class.java.simpleName, msg(::DidEnter.name))
	}

	abstract fun value():DIDArticle.EStatus

	suspend fun SetStateToNext(newState: DIDArticleState, onlyDB:Boolean = false):Error? {
		Log.d(DIDArticleState::class.java.simpleName, msg("${::SetStateToNext.name} oldState=${this::class.java.simpleName} newState=${newState::class.java.simpleName} start"))

		newState.address = this.address
		newState.context = this.context
		newState.owner = this.owner

		this.WillLeave()

		val err = owner.SetStateTo(address, this, newState, onlyDB)
		if (err!=null) {
			Log.w(DIDArticleState::class.java.simpleName, msg("${::SetStateToNext.name} oldState=${this::class.java.simpleName} newState=${newState::class.java.simpleName} err=${err}"))
			return err
		}

		newState.DidEnter()

		Log.d(DIDArticleState::class.java.simpleName, msg("${::SetStateToNext.name} oldState=${this::class.java.simpleName} newState=${newState::class.java.simpleName} end"))

		return null
	}
}

class DIDArticleStateWaitToken: DIDArticleState() {
	override fun value(): DIDArticle.EStatus {
		return DIDArticle.EStatus.WaitToken
	}
}

class DIDArticleStateDelete : DIDArticleState() {
	override fun value(): DIDArticle.EStatus {
		return DIDArticle.EStatus.Delete
	}
}

class DIDArticleStateNone : DIDArticleState() {
	override fun value(): DIDArticle.EStatus {
		return DIDArticle.EStatus.None
	}
}
class DIDArticleStateEditing : DIDArticleState() {
	override fun value(): DIDArticle.EStatus {
		return DIDArticle.EStatus.Editing
	}

	override suspend fun Upload(): Error? {
		return this.SetStateToNext(DIDArticleStateUploading())
	}
}

class DIDArticleStateUploading : DIDArticleState() {
	override fun value(): DIDArticle.EStatus {
		return DIDArticle.EStatus.Uploading
	}

}

class DIDArticleStateUploadFail : DIDArticleState() {
	override fun value(): DIDArticle.EStatus {
		return DIDArticle.EStatus.UploadFail
	}

	override suspend fun Upload(): Error? {
		return this.SetStateToNext(DIDArticleStateUploading())
	}
}

class DIDArticleStateUploadOk : DIDArticleState() {
	override fun value(): DIDArticle.EStatus {
		return DIDArticle.EStatus.UploadOk
	}

}

class DIDArticleStateConfirming : DIDArticleState() {
	override fun value(): DIDArticle.EStatus {
		return DIDArticle.EStatus.Confirming
	}

}

class DIDArticleStateConfirmOk : DIDArticleState() {
	override fun value(): DIDArticle.EStatus {
		return DIDArticle.EStatus.ConfirmOk
	}

	override suspend fun DidEnter() {
		super.DidEnter()

		DIDArticle.findByAddress(address)?.apply {
			if (this.EditingBlockRootHash.isEmpty()) {
				require(this.Context.isEmpty()) {"Address(${address})'s EditingBlockRootHash is empty, but Context isNotEmpty"}
				require(this.Title.isEmpty()) {"Address(${address})'s EditingBlockRootHash is empty, but Title isNotEmpty"}
				return@apply
			}

			this.Address = address
			this.EditingBlockRootHash = ""
			this.Context = ""
			this.Title = ""
			this.CurrentBlockRootHash = this.EditingBlockRootHash
			this.update(mutableListOf(DIDArticle.CurrentBlockRootHash, DIDArticle.EditingBlockRootHash, DIDArticle.Context, DIDArticle.Title))
			getUs().nc.postToMain(DIDArticle.ChangedEvent(address))
			DIDArticle.ChangedEvent().apply {
				this.ids = listOf(address)
				getUs().nc.postToMain(this)
			}
		}
	}
}

class DIDArticleStateConfirmFail : DIDArticleState() {
	override fun value(): DIDArticle.EStatus {
		return DIDArticle.EStatus.ConfirmFail
	}
}

class DIDArticleStateWaitAbstract : DIDArticleState() {
	override fun value(): DIDArticle.EStatus {
		return DIDArticle.EStatus.WaitAbstract
	}

	override suspend fun EditAbstract(): Error? {
//		ARouter.getInstance().build(Router.Did.feeSetting).withString(Constants.PARAM, address).navigation()
		return null
	}

}

class DIDArticleStateDone : DIDArticleState() {
	override suspend fun Confirm(signResults: MutableList<SignResult>): Error? {
		return null
	}
	override fun value(): DIDArticle.EStatus {
		return DIDArticle.EStatus.Done
	}

	override suspend fun EditAbstract(): Error? {
//		ARouter.getInstance().build(Router.Did.feeSetting).withString(Constants.PARAM, address).navigation()
		return null
	}

	override suspend fun Upload(): Error? {
		return this.SetStateToNext(DIDArticleStateUploading())
	}

}

suspend fun BuildDIDArticleState(context: Context, address:String):DIDArticleState {
	require(address.isNotEmpty()) {"address(${address}) can not be empty."}

	val map:MutableMap<DIDArticle.EStatus, DIDArticleState> = mutableMapOf(
		Pair(DIDArticleStateNone().value(), DIDArticleStateNone()),
		Pair(DIDArticleStateEditing().value(), DIDArticleStateEditing()),
		Pair(DIDArticleStateUploading().value(), DIDArticleStateUploading()),
		Pair(DIDArticleStateUploadFail().value(), DIDArticleStateUploadFail()),
		Pair(DIDArticleStateUploadOk().value(), DIDArticleStateUploadOk()),
		Pair(DIDArticleStateConfirming().value(), DIDArticleStateConfirming()),
		Pair(DIDArticleStateConfirmOk().value(), DIDArticleStateConfirmOk()),
		Pair(DIDArticleStateConfirmFail().value(), DIDArticleStateConfirmFail()),
		Pair(DIDArticleStateWaitAbstract().value(), DIDArticleStateWaitAbstract()),
		Pair(DIDArticleStateDone().value(), DIDArticleStateDone()),
		Pair(DIDArticleStateDelete().value(), DIDArticleStateDelete()),
		Pair(DIDArticleStateWaitToken().value(), DIDArticleStateWaitToken()),
	)

	val owner = object : DIDArticleStateOwner {
		override suspend fun SetStateTo(address:String, oldState: DIDArticleState, newState: DIDArticleState, onlyDB: Boolean): Error? {
			require(oldState.address == newState.address) {"oldState(${oldState}) and newState(${newState})'s address must be same."}
			require(address == newState.address) {"address(${address}) and newState(${newState})'s address must be same."}

			Log.d(DIDArticleState::class.java.simpleName, "address=${newState.address} SetStateTo oldState=${oldState} newState=${newState} onlyDB=${onlyDB} start")

			if (onlyDB) {
				val err = DIDArticle.SetStateTo(address, oldState.value().int, newState.value().int)
				Log.d(DIDArticleState::class.java.simpleName, "address=${newState.address} SetStateTo oldState=${oldState} newState=${newState} onlyDB=${onlyDB} end, err=${err}")
				if (err==null) {
					getUs().nc.postToMain(DIDArticle.ChangedEvent(address))
					DIDArticle.ChangedEvent().apply {
						this.ids = listOf(address)
						getUs().nc.postToMain(this)
					}
					return null
				}
				DIDArticle.findByAddress(address)?.apply {
					return BuildDIDArticleState(context, address).SetStateToNext(newState, true)
				}

				return Error("not found")
			}


			return Error("result is ")
		}
	}
	var state:DIDArticleState = DIDArticleStateNone()
	DIDArticle.findByAddress(address)?.apply {
		DIDArticle.EStatus.valueOf(this.Status)?.apply {
			state = map[this]?:throw Exception("BuildDIDArticleState address(${address}) status=${this}")
		}
	}
	state.context = context
	state.address = address
	state.owner = owner
	Log.d(DIDArticleState::class.java.simpleName, "address=${address} BuildDIDArticleState state=${state::class.java.simpleName}")
	return state
}