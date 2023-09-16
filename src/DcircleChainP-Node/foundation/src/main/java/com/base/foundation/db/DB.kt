package com.base.foundation.db

import android.os.Build
import com.base.foundation.getAppContext
import com.tencent.wcdb.FileUtils
import java.io.File


fun GetDBPath(name:String): String {
	val mDataDir: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
		getAppContext().dataDir.absolutePath
	} else {
		//安卓7.0一下兼容
		getAppContext().cacheDir.absolutePath
	}
	val mDataDirFile = File(mDataDir)
	var mDatabasesDir = File(mDataDirFile, "databases")
	if (mDatabasesDir.path == "databases") {
		mDatabasesDir = File("/data/system")
	}

	// 需要生成 databases 的路径
	if (!mDatabasesDir.isDirectory && mDatabasesDir.mkdir()) {
		FileUtils.setPermissions(mDatabasesDir.path, 505, -1, -1)
	}

	return mDatabasesDir.path + File.separator + name
}