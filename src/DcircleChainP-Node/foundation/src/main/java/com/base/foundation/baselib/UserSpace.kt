package com.base.foundation.baselib

import android.content.Context
import android.util.Log
import com.base.foundation.DCircleEnv
import com.base.foundation.Env
import com.base.foundation.api.Net
import com.base.foundation.api.NetFactory
import com.base.foundation.db.DownloadTask
import com.base.foundation.db.FindByBaseUrl
import com.base.foundation.db.FindOneMaybeActive
import com.base.foundation.db.GetDBPath
import com.base.foundation.db.Me
import com.base.foundation.db.NetConfig
import com.base.foundation.db.Token
import com.base.foundation.db.WCDBAdapter
import com.base.foundation.nc.NC
import com.base.foundation.oss.OSSNetDownloaderFactory
import com.base.foundation.oss.OSSNetDownloaderGetter
import com.github.xpwu.ktdbtable.DBQueue
import com.tencent.wcdb.database.SQLiteDatabase

interface UsNfInterface {
	fun get(name:String?=null): Net
    fun del(net:Net)
}

class UsNf(var baseUrl: String,var us: UserSpace) : UsNfInterface {
	var nf : NetFactory = NetFactory()
    override fun get(name: String?): Net {
		var netName = name
        if (name==null) {
			netName = "main"
		}

		val token = Token()
        token.id = netName!!
		val ret = nf.get(netName, token)
		ret.setBaseUrl(this.baseUrl)
		return ret
    }

	override fun del(net: Net) {
		nf.del(net)
	}
}

interface UserSpace {
	var selfDB: DBQueue<SQLiteDatabase>
	var shareDB: DBQueue<SQLiteDatabase>
	var downloader: OSSNetDownloaderGetter
	suspend fun clone(uid:String):UserSpace

	var nc: NC

	var nf: UsNf

	fun isValid():Boolean

	fun getUid():String
}

class AloneUserSpaceSync(var context: Context, var baseUrl: String, override var shareDB: DBQueue<SQLiteDatabase>
) : UserSpace {
	private var uid:String = ""
	private var valid:Boolean = false
	override var downloader: OSSNetDownloaderGetter = object : OSSNetDownloaderGetter {
		override suspend fun AddTask(task: DownloadTask): Error {
			return Error("downloader not ready, please check you logic")
		}

		override suspend fun AddTask(task: MutableList<DownloadTask>): Error {
			return Error("downloader not ready, please check you logic")
		}

		override fun Close() {
		}

		override suspend fun Start(us:UserSpace) {
		}
	}
	override var selfDB: DBQueue<SQLiteDatabase> = DBQueue {
		var name = "u_self_is_useless.db"
		if (Env != DCircleEnv.pro) {
			name = "u_self_is_useless.${Env}.db"
		}
		val sql = SQLiteDatabase.openOrCreateDatabase(GetDBPath(name), null)
		sql.enableWriteAheadLogging()
        return@DBQueue  WCDBAdapter(sql)
	}

    override suspend fun clone(uid: String):UserSpace {
		Log.d(this::class.java.simpleName, "clone uid=${uid}, old uid=${this.uid}")
		require(uid.isNotEmpty()) {"${::AloneUserSpaceSync::class.java.simpleName} clone uid(${uid}) can not be empty."}
		if (uid == this.uid) {
			return this
        }

		NetConfig.FindByBaseUrl(this.baseUrl)?.apply {
			if (this.ActiveTime<=0) {
				NetConfig.FindOneMaybeActive(NetConfig.EBusiness.AppServer)?.apply {
					this@AloneUserSpaceSync.baseUrl = this.BaseUrl
					this@AloneUserSpaceSync.nf = UsNf(this.BaseUrl, this@AloneUserSpaceSync)
				}
			}
		}

		val newUs = AloneUserSpaceSync(context, this.baseUrl, this.shareDB)
        newUs.valid = true
		newUs.uid = uid
        newUs.downloader = OSSNetDownloaderFactory(newUs)

		var name = "u_${uid}.db"
		if (Env != DCircleEnv.pro) {
			name = "u_${uid}.${Env}.db"
		}
		val sql = SQLiteDatabase.openOrCreateDatabase(GetDBPath(name), null)
		sql.enableWriteAheadLogging()
        newUs.selfDB =  DBQueue {
			return@DBQueue WCDBAdapter(sql)
		}

		this.uid = Me.empty
		this.valid = false
        this.downloader.Close()

		newUs.downloader.Start(newUs)

		return newUs
    }

	override var nc = NC()
	override var nf = UsNf(this.baseUrl, this)

	override fun isValid(): Boolean {
		return valid&&uid!=Me.empty
	}

	override fun getUid(): String {
		return uid
    }
}
