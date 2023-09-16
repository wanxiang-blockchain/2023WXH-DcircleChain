package com.base.foundation.db

import android.content.ContentValues
import com.base.foundation.getUs
import com.base.foundation.nc.NcEvent
import com.github.xpwu.ktdbtable.*
import com.github.xpwu.ktdbtable.where.and
import com.github.xpwu.ktdbtble.annotation.Column
import com.github.xpwu.ktdbtble.annotation.PrimaryKey
import com.github.xpwu.ktdbtble.annotation.Table
import com.tencent.wcdb.database.SQLiteDatabase

private val table = ChatCmd.Companion
@Table("ChatCmd")
open class ChatCmd {
	companion object

    class NewEvent : NcEvent<String>()

    @Column("id", primaryKey = PrimaryKey.ONLY_ONE)
	var id:String = ""
	
	@Column("chatId")
	var chatId: String = ""
	
	@Column("cmd")
	var cmd: Int = 0
	
	@Column("data")
	var data: String = ""
	
	@Column("seq")
	var seq: Int = 0

	@Column("done")
	var done:Int = 0

	enum class Status(val value: Int) {
		Wait(0),
		Ok(1),
		Doing(2)
	}
}

fun ChatCmd.Companion.BuildId(chatId:String, seq:Int):String {
	return "${chatId}_${seq}"
}

suspend fun Array<ChatCmd>.insert(): Array<ChatCmd> {
	return getUs().selfDB {
		val failedList: MutableList<ChatCmd> = mutableListOf()
		it.UnderlyingDB.beginTransaction()
		try {
			for (data in this) {
				try {
					data.id = ChatCmd.BuildId(data.chatId, data.seq)//"${data.chatId}_${data.seq}"
					it.UnderlyingDB.insertOrThrow(table.TableNameIn(it), null, data.ToContentValues())
				} catch (e: Exception) {
					failedList.add(data)
				}
			}
			it.UnderlyingDB.setTransactionSuccessful()
		} finally {
			it.UnderlyingDB.endTransaction()
		}
		return@selfDB failedList.toTypedArray()
	}
}

suspend fun ChatCmd.Companion.FindNotExecuteChatId():Array<String> {
	return getUs().selfDB { it ->
        val columns = listOf(ChatCmd.chatId.name).toTypedArray()
		val name = table.TableNameIn(it)
		val where = ChatCmd.done.eq(ChatCmd.Status.Wait.value)
		val cursor = it.UnderlyingDB.query(
			name,
			columns,
			where.ArgSQL,
			where.BindArgs,
			null,
			null,
			"${ChatCmd.seq} DESC"
		)
		val result = mutableListOf<ChatCmd>()
		if (!cursor.moveToFirst()) {
			cursor.close()
            return@selfDB result.map { it.chatId }.toTypedArray()
        }
		do {
			val doc = ChatCmd()
            cursor.ToChatCmd(doc)
            result.add(doc)
		} while (cursor.moveToNext())
		cursor.close()
        return@selfDB result.map { it.chatId }.toTypedArray()
    }
}

suspend fun Array<ChatCmd>.delete() {
	return getUs().selfDB { it ->
        val ids = this.map { "${it.chatId}_${it.seq}" }.toTypedArray()

		val value = ContentValues().apply {
			this.put(ChatCmd.done.name, ChatCmd.Status.Ok.value)
		}

		val where = ChatCmd.id.`in`(ids)
		it.UnderlyingDB.update(table.TableNameIn(it), value, where.ArgSQL, where.BindArgs)
	}
}

suspend fun ChatCmd.Companion.Delete(chatId:String, max:Int) {
	return getUs().selfDB {
		val name = table.TableNameIn(it)
		val where = ChatCmd.chatId.eq(chatId) and ChatCmd.seq.lte(max)
		val value = ContentValues().apply {
			this.put(ChatCmd.done.name, ChatCmd.Status.Ok.value)
		}

		it.UnderlyingDB.update(name,value, where.ArgSQL, where.BindArgs)
	}
}

suspend fun ChatCmd.Companion.FindByChatId(chatId:String, limit:Int=999): Array<ChatCmd> {
	return getUs().selfDB { it ->
        val columns = ChatCmd.AllColumns().map { it.name }.toTypedArray()
		val name = table.TableNameIn(it)
		val where = ChatCmd.chatId.eq(chatId) and ChatCmd.done.eq(ChatCmd.Status.Wait.value)
		val cursor = it.UnderlyingDB.query(
				name,
				columns,
				where.ArgSQL,
				where.BindArgs,
				null,
				null,
				"${ChatCmd.seq} DESC",
			limit.toString()
			)
		val result = mutableListOf<ChatCmd>()
		if (!cursor.moveToFirst()) {
			cursor.close()
            return@selfDB result.toTypedArray()
        }
		do {
			val doc = ChatCmd()
            cursor.ToChatCmd(doc)
            result.add(doc)
		} while (cursor.moveToNext())
		cursor.close()
        return@selfDB result.toTypedArray()
    }
}

suspend fun ChatCmd.update(columns:List<ColumnInfo>): Error? {
	return getUs().selfDB {
		return@selfDB this.update(it, columns)
	}
}

fun ChatCmd.update(it:DB<SQLiteDatabase>, columns:List<ColumnInfo>): Error? {
	val where = ChatCmd.id.eq(this.id)
	val name = table.TableNameIn(it)
	try {
		val colNum = it.UnderlyingDB.update(
			name,
			this.ToContentValues(columns),
			where.ArgSQL,
			where.BindArgs
		)

		if (colNum <= 0) {
			return Error("not found")
		}
		return null
	} catch (e: Exception) {
		return Error(e)
    }
}

suspend fun ChatCmd.Companion.SetAllStatus(oldStatus:ChatCmd.Status /*当前的状态*/, newStatus:ChatCmd.Status /*新的状态*/): Error? {
	return getUs().selfDB {
		val name = table.TableNameIn(it)
		val where = ChatCmd.done.eq(oldStatus.value)
		val value = ContentValues().apply {
			this.put(ChatCmd.done.name, newStatus.value)
		}

		try {
			it.UnderlyingDB.update(name,value, where.ArgSQL, where.BindArgs)
		} catch (e: Exception) {
			return@selfDB Error(e)
		}

		return@selfDB null
	}
}
