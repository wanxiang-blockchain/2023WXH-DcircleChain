package com.base.foundation.db
import android.content.ContentValues
import android.database.Cursor
import com.github.xpwu.ktdbtable.DBer
import com.tencent.wcdb.database.SQLiteDatabase

class WCDBAdapter(override val UnderlyingDB: SQLiteDatabase) : DBer<SQLiteDatabase> {
	override fun ExecSQL(sql: String) {
		UnderlyingDB.execSQL(sql)
	}

	override fun Query(query: String, bindArgs: Array<String>?): Cursor {
		return UnderlyingDB.rawQuery(query, bindArgs)
	}

	override fun Replace(table: String, values: ContentValues): Long {
		return UnderlyingDB.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE)
	}

	override fun BeginTransaction() {
		UnderlyingDB.beginTransaction()
	}

	override fun SetTransactionSuccessful() {
		UnderlyingDB.setTransactionSuccessful()
	}

	override fun EndTransaction() {
		UnderlyingDB.endTransaction()
	}
}