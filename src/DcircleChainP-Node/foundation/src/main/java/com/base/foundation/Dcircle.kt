package com.base.foundation

import android.content.Context
import android.util.Log
import com.base.foundation.baselib.AloneUserSpaceSync
import com.base.foundation.baselib.UserSpace
import com.base.foundation.db.DownloadTask
import com.base.foundation.db.FindDownloadingTask
import com.base.foundation.db.GetDBPath
import com.base.foundation.db.WCDBAdapter
import com.github.xpwu.ktdbtable.DBQueue
import com.tencent.wcdb.database.SQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val demoMnemonic = "timber female huge avoid autumn acid solution humble tackle analyst music news"
const val demoPassword = "Aa123456"
const val demoPackageName = "demo.web3.social"
var DCircleScope = CoroutineScope(Dispatchers.Main)
var NetScope = CoroutineScope(Dispatchers.IO)
var userSpace:UserSpace? = null
fun getUs():UserSpace {
	if (userSpace ==null) {
		throw Error("Please setAppContext when app launched.")
    }
	return userSpace as UserSpace
}

fun setUs(us: UserSpace) {
	userSpace = us

	CoroutineScope(Dispatchers.IO).launch {
		val tasks = DownloadTask.FindDownloadingTask()
		for (doc in tasks) {
			us.downloader.AddTask(doc)
		}
	}
}

var appContext_: Context?=null
fun setAppContext(context:Context) {
	Log.d("Dcircle", "setAppContext context=${context}")

	appContext_ = context
	userSpace = AloneUserSpaceSync(context, BaseUrl, DBQueue {
		var name = "share.db"
		if (Env != DCircleEnv.pro) {
			name = "share.${Env}.db"
		}
		val sql = SQLiteDatabase.openOrCreateDatabase(GetDBPath(name), null)
		sql.enableWriteAheadLogging()
        return@DBQueue  WCDBAdapter(sql)
	})
}

fun getAppContext():Context {
	if (appContext_ ==null) {
		throw Error("Please setAppContext when app launched.")
    }

	return appContext_ as Context
}

class PPKey {
	var privateKey:String = ""
	var publicKey:String = ""
	var address:String = ""

	fun isValid(): Boolean {
		return privateKey.isNotEmpty()
	}
}

val walletKey_:MutableMap<String, PPKey> = mutableMapOf()
fun setWalletKey(key:PPKey) {
	walletKey_[getUs().getUid()] = key
}

fun getWalletKey():PPKey {
	return walletKey_[getUs().getUid()] ?: PPKey()
}