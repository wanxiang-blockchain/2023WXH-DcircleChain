package com.base.foundation.db

import android.util.Log
import com.base.foundation.getUs
import com.github.xpwu.ktdbtable.eq
import com.github.xpwu.ktdbtable.gt
import com.github.xpwu.ktdbtable.invoke
import com.github.xpwu.ktdbtable.where.and
import com.github.xpwu.ktdbtble.annotation.Column
import com.github.xpwu.ktdbtble.annotation.PrimaryKey
import com.github.xpwu.ktdbtble.annotation.Table
import com.tencent.wcdb.database.SQLiteDatabase

private val table = NetConfig.Companion
@Table("NetConfig")
class NetConfig {
	@Column("BaseUrl", primaryKey = PrimaryKey.ONLY_ONE)
	var BaseUrl: String = ""

	@Column("ActiveTime")
	var ActiveTime: Long = 0	// 0--不可访问 1--有可能还可以访问 >1 -- 最近一次访问时间

	@Column("TTL")
	var TTL:Int = 0

	@Column("Business")
	var Business:Int = EBusiness.AppServer.int
	enum class EBusiness(val int: Int) {
		AppServer(0),
		ShareLink(1),
	}

	companion object
}

suspend fun NetConfig.insert(): Error? {
	return getUs().shareDB {
		try {
			val name = table.TableNameIn(it)
			val values = this.ToContentValues()
			it.UnderlyingDB.insertOrThrow(name, null, values)
			return@shareDB null
        } catch (e: Exception) {
			return@shareDB Error(e)
		}
	}
}

suspend fun Array<NetConfig>.insert(): Array<String> {
	return getUs().shareDB {
		val db = it.UnderlyingDB
        val name = table.TableNameIn(it)
		val failedList: MutableList<String> = mutableListOf()
		db.beginTransaction()
		try {
			for (data in this) {
				try {
					db.insertOrThrow(name, null, data.ToContentValues())
				} catch (e: Exception) {
					failedList.add(data.BaseUrl)
				}
			}
			db.setTransactionSuccessful()
		} finally {
			db.endTransaction()
		}
		return@shareDB failedList.toTypedArray()
	}
}

suspend fun NetConfig.setActiveTime():Error? {
	return getUs().shareDB {
		val columns = mutableListOf(NetConfig.ActiveTime, NetConfig.TTL, NetConfig.Business)
		val where =  NetConfig.BaseUrl.eq(this.BaseUrl)
		val name = table.TableNameIn(it)
		try {
			val colNum = it.UnderlyingDB.updateWithOnConflict(
				name,
				this.ToContentValues(columns),
				where.ArgSQL,
				where.BindArgs,
				SQLiteDatabase.CONFLICT_IGNORE
			)
			Log.d(NetConfig::class.java.simpleName, "SetActiveTime BaseUrl=${this.BaseUrl} ActiveTime=${this.ActiveTime} TTL=${this.TTL}")
			if (colNum <= 0) {
				return@shareDB Error("not found")
			}
			return@shareDB null
		} catch (e: Exception) {
			return@shareDB Error(e)
        }
	}
}

suspend fun NetConfig.delete():Error? {
	return getUs().shareDB {
		val where =  NetConfig.BaseUrl.eq(this.BaseUrl)
		val name = table.TableNameIn(it)
		try {
			val colNum = it.UnderlyingDB.delete(
				name,
				where.ArgSQL,
				where.BindArgs,
			)
			if (colNum <= 0) {
				return@shareDB Error("not found")
			}
			return@shareDB null
		} catch (e: Exception) {
			return@shareDB Error(e)
        }
	}
}

suspend fun NetConfig.Companion.FindOneMaybeActive(business:NetConfig.EBusiness): NetConfig? {
	return getUs().shareDB.invoke { it ->
        val name = table.TableNameIn(it)
		try {
			val columns = NetConfig.AllColumns().map { it.name }.toTypedArray()
			val where = NetConfig.ActiveTime.gt(1) and NetConfig.Business.eq(business.int)
			val cursor = it.UnderlyingDB.query(
				name,
				columns,
				where.ArgSQL,
				where.BindArgs,
				null,
				null,
				null
			)
			if (!cursor.moveToFirst()) {
				cursor.close()
                return@invoke null
			}

			val doc = NetConfig()
            cursor.ToNetConfig(doc)
			cursor.close()
			return@invoke doc
		} catch (e: Exception) {
			return@invoke null
		}
	}
}

suspend fun NetConfig.Companion.FindByBaseUrl(baseUrl: String): NetConfig? {
	return getUs().shareDB { it ->
        val columns = NetConfig.AllColumns().map { it.name }.toTypedArray()
		val name = table.TableNameIn(it)
		val where = NetConfig.BaseUrl.eq(baseUrl)
		val cursor =
			it.UnderlyingDB.query(
				name,
				columns,
				where.ArgSQL,
				where.BindArgs,
				null,
				null,
				null
			)
		if (!cursor.moveToFirst()) {
			cursor.close()
            return@shareDB null
        }
		val doc = NetConfig()
        cursor.ToNetConfig(doc)
        cursor.close()
        return@shareDB doc
    }
}

fun NetConfig.Companion.Initializer(): Collection<NetConfig> {
	return listOf()
}

suspend fun NetConfig.Companion.FindAllDocs(): Array<NetConfig> {
	return getUs().shareDB { it ->
        val columns = NetConfig.AllColumns().map { it.name }.toTypedArray()
		val name = table.TableNameIn(it)
		val cursor =
			it.UnderlyingDB.query(
				name,
				columns,
				null,
				null,
				null,
				null,
				null
			)
		val result = mutableListOf<NetConfig>()
		if (!cursor.moveToFirst()) {
			cursor.close()
            return@shareDB result.toTypedArray()
        }
		do {
			val doc = NetConfig()
            cursor.ToNetConfig(doc)
            result.add(doc)
		} while (cursor.moveToNext())
		cursor.close()
        return@shareDB result.toTypedArray()
    }
}
