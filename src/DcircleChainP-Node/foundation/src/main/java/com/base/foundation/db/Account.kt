package com.base.foundation.db

import android.util.Log
import com.base.foundation.Aes
import com.base.foundation.getUs
import com.base.foundation.nc.NcEvent
import com.base.foundation.oss.getObjectHash
import com.base.foundation.utils.toHex
import com.github.xpwu.ktdbtable.ColumnInfo
import com.github.xpwu.ktdbtable.eq
import com.github.xpwu.ktdbtable.invoke
import com.github.xpwu.ktdbtble.annotation.Column
import com.github.xpwu.ktdbtble.annotation.PrimaryKey
import com.github.xpwu.ktdbtble.annotation.Table
import com.tencent.wcdb.database.SQLiteDatabase
import java.util.*

private val table = Account.Companion
@Table("Account")
open class Account {
	class ChangedEvent : NcEvent<String>()

    @Column("address", primaryKey = PrimaryKey.ONLY_ONE)
	var address:String = ""

	@Column("mnemonic")
	var mnemonic:String = ""

	@Column("guardValue")
	var guardValue:String = ""

	@Column("iv")
	var iv:String = ""

	@Column("password")
	var password:String = ""

	@Column("walletAddress")
	var walletAddress:String = ""

	@Column("walletSign")
	var walletSign:String = ""

	enum class From(var int: Int) {
		CreateMnemonic(1),
		Wallet(2),
		ImportMnemonic(3),
	}

	@Column("_from_")
	var from:Int = 0

	@Column("createTime")
	var createTime:Long = 0

	enum class Status(var int: Int) {
		UserSetPassword(0),
		DefaultPassword(1),
	}

	@Column("status")
	var status:Int = Status.UserSetPassword.int

	companion object
}

suspend fun Account.Companion.find(limit:Int = Int.MAX_VALUE): Array<Account> {
	return getUs().shareDB.invoke { it ->
        val docs = mutableListOf<Account>()
		val name = table.TableNameIn(it)
		try {
			val columns = Account.AllColumns().map { it.name }.toTypedArray()
			val cursor = it.UnderlyingDB.query(
				name,
				columns,
				null,
				null,
				null,
				null,
				"${Account.createTime.name} DESC",
				limit.toString()
			)
			if (!cursor.moveToFirst()) {
				cursor.close()
                return@invoke arrayOf()
            }

			do {
				val doc = Account()
                cursor.ToAccount(doc)
				docs.add(doc)
            } while (cursor.moveToNext())
			cursor.close()
			return@invoke docs.toTypedArray()
		} catch (e: Exception) {
			return@invoke arrayOf()
		}
	}
}

suspend fun Account.Companion.findByAddress(address:String):Account? {
	return getUs().shareDB.invoke { it ->
        val name = table.TableNameIn(it)
		val where = Account.address.eq(address)
		try {
			val columns = Account.AllColumns().map { it.name }.toTypedArray()
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

			val doc = Account()
            cursor.ToAccount(doc)
			cursor.close()
			return@invoke doc
		} catch (e: Exception) {
			return@invoke null
		}
	}
}

suspend fun Account.insert(): Error? {
	require(this.mnemonic.split(" ").size != 12) {
		"mnemonic(${this.mnemonic}) is unencrypted, you logic somewhere is wrong"
	}

	this.createTime = Date().time
	return getUs().shareDB {
		try {
			val values = this.ToContentValues()
			val rowId = it.UnderlyingDB.insertOrThrow(table.TableNameIn(it), null, values)
			Log.d(Account::class.java.simpleName, "insert rowId=${rowId}")
			return@shareDB null
        } catch (e: Exception) {
			Log.e(Account::class.java.simpleName, "Insert Exception=${e}")
			return@shareDB Error(e)
		}
	}
}

suspend fun Account.update(columns:List<ColumnInfo> = listOf(Account.createTime)): Error? {
	return getUs().shareDB.invoke {
		val where = Account.address.eq(this.address)
		val name = table.TableNameIn(it)
		try {
			val colNum = it.UnderlyingDB.updateWithOnConflict(
				name,
				this.ToContentValues(columns),
				where.ArgSQL,
				where.BindArgs,
				SQLiteDatabase.CONFLICT_IGNORE
			)
			if (colNum <= 0) {
				return@invoke Error("not found")
			}
			getUs().nc.postToMain(Account.ChangedEvent())
			return@invoke null
		} catch (e: Exception) {
			return@invoke Error(e)
        }
	}

}

suspend fun Account.delete():Error? {
	return getUs().shareDB {
		try {
			val where = Account.address.eq(this.address)
			val rowId = it.UnderlyingDB.delete(table.TableNameIn(it), where.ArgSQL, where.BindArgs)
			if (rowId<=0) {
				return@shareDB Error("not found")
			}
			return@shareDB null
        } catch (e: Exception) {
			return@shareDB Error(e)
		}
	}
}

fun Account.Companion.getPassword(password: String): Aes {
	val hash = getObjectHash(password.toByteArray(Charsets.UTF_8))
	return Aes(toHex(hash.slice(0 until 16).toByteArray()))
}