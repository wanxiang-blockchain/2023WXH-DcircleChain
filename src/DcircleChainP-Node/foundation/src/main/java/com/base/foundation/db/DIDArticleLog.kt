package com.base.foundation.db

import com.base.foundation.getUs
import com.base.foundation.nc.NcEvent
import com.base.thridpart.toJson
import com.blankj.utilcode.util.LogUtils
import com.github.xpwu.ktdbtable.ColumnInfo
import com.github.xpwu.ktdbtable.eq
import com.github.xpwu.ktdbtable.invoke
import com.github.xpwu.ktdbtble.annotation.*
import com.github.xpwu.ktdbtble.annotation.Table
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

private val table = DIDArticleLog.Companion

@Table("DIDArticleLog")
class DIDArticleLog {
	class ChangedEvent(var address:String="") : NcEvent<String>(listOf(address)) {
		override fun getName(): String {
			return super.getName() + address
		}
	}

	companion object;

    @SerializedName("Address")
	@Column("Address", primaryKey = PrimaryKey.ONLY_ONE)
	var Address: String = ""

	class Item {
		enum class UpdateType(var int: Int) {
			Content(1),
			Abstract(2),
			TokenAddress(3),
		}

		var updateType:Int = 0
		var version:Int = 0
	}

	@SerializedName("Items")
	@Column("Items")
	var Items:Array<Item> = arrayOf()
}

@FromByteArray
fun DIDArticleLog_Item_FromByteArray(byteArray: ByteArray):Array<DIDArticleLog.Item> {
	return Gson().fromJson(String(byteArray, Charsets.UTF_8), Array<DIDArticleLog.Item>::class.java)
}

@ToByteArray
fun DIDArticleLog_Item_ToByteArray(items:Array<DIDArticleLog.Item>):ByteArray {
	return Gson().toJson(items).toByteArray(Charsets.UTF_8)
}

suspend fun DIDArticleLog.insert(): Error? {
	return getUs().selfDB.invoke {
		val values = this.ToContentValues()
		val name = table.TableNameIn(it)
		try {
			it.UnderlyingDB.insertOrThrow(name, null, values)
			return@invoke null
        } catch (e: Exception) {
			LogUtils.w("DIDArticleLog.Insert err", e.toString(), this.toJson())
			return@invoke Error(e)
		}
	}
}

suspend fun Array<DIDArticleLog>.insert(): Array<String> {
	return getUs().selfDB.invoke {
		val failedList: MutableList<String> = mutableListOf()
		val name = table.TableNameIn(it)
		it.UnderlyingDB.beginTransaction()
		try {
			for (data in this) {
				try {
					it.UnderlyingDB.insertOrThrow(name, null, data.ToContentValues())
				} catch (e: Exception) {
					failedList.add(data.Address)
				}
			}
			it.UnderlyingDB.setTransactionSuccessful()
		} finally {
			it.UnderlyingDB.endTransaction()
		}
		return@invoke failedList.toTypedArray()
	}
}

suspend fun DIDArticleLog.Companion.FindById(address: String): Array<DIDArticleLog.Item> {
	return getUs().selfDB.invoke { it ->
        val where = DIDArticleLog.Address.eq(address)
		val cursor =
			it.UnderlyingDB.query(
				table.TableNameIn(it),
				DIDArticleLog.AllColumns().map { it.name }.toTypedArray(),
				where.ArgSQL,
				where.BindArgs,
				null,
				null,
				null
			)
		if (!cursor.moveToFirst()) {
			cursor.close()
            return@invoke arrayOf()
        }
		val doc = DIDArticleLog()
        cursor.ToDIDArticleLog(doc)
        cursor.close()
        return@invoke doc.Items
    }
}

suspend fun Array<DIDArticleLog>.update(columns: List<ColumnInfo> = listOf(DIDArticleLog.Items)): Array<String> {
	val failed: MutableList<String> = mutableListOf()
    for (entity in this) {
		if (entity.update(columns) != null) {
			failed.add(entity.Address)
		}
	}
	return failed.toTypedArray()
}

suspend fun DIDArticleLog.update(columns: List<ColumnInfo> = listOf(DIDArticleLog.Items)): Error? {
	return getUs().shareDB {
		val where = DIDArticleLog.Address.eq(this.Address)
		try {
			val colNum = it.UnderlyingDB.update(
				table.TableNameIn(it),
				this.ToContentValues(columns),
				where.ArgSQL,
				where.BindArgs
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
