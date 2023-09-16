package com.base.foundation.db

import com.base.foundation.getUs
import com.base.foundation.nc.NcEvent
import com.github.xpwu.ktdbtable.ColumnInfo
import com.github.xpwu.ktdbtable.eq
import com.github.xpwu.ktdbtable.invoke
import com.github.xpwu.ktdbtble.annotation.Column
import com.github.xpwu.ktdbtble.annotation.Index
import com.github.xpwu.ktdbtble.annotation.PrimaryKey
import com.github.xpwu.ktdbtble.annotation.Table

private val table = User.Companion
@Table("User")
open class User {

    class ChangedEvent(private var uidS: List<String>) : NcEvent<String>(uidS) {
        var uid:String = ""
        constructor() : this(listOf())

        constructor(uid:String) : this(listOf(uid)) {
            this.uid = uid
        }

        override fun getName(): String {
            return super.getName() + uid
        }
    }

    companion object {
        const val TAG = "User"
        fun getName(doc:User):String {
            if (doc.Name.isNotEmpty()) {
                return doc.Name
            }

            if (doc.Uid.length<=6) {
                return doc.Uid
            }

            return doc.Uid.substring(doc.Uid.length-6 until doc.Uid.length)
        }
    }

    @Column("uid", primaryKey = PrimaryKey.ONLY_ONE)
    var Uid: String = ""

    @Column("pubkey")
    var Pubkey: String = ""

    @Column("inviteCode")
    var InviteCode: String = ""

    @Column("nameEn")
    var NameEn: String = ""

    @Column("name")
    @Index
    var Name: String = ""

    @Column("avatar128")
    var Avatar128: String = ""

    @Column("avatar640")
    var Avatar640: String = ""
}

suspend fun User.Companion.FindById(id: String): User? {
    return getUs().shareDB { it ->
        val where = User.Uid.eq(id)
        val cursor =
            it.UnderlyingDB.query(
                table.TableNameIn(it),
                User.AllColumns().map { it.name }.toTypedArray(),
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
        val doc = User()
        cursor.ToUser(doc)
        cursor.close()
        return@shareDB doc
    }
}

suspend fun User.insert(): Error? {
    return getUs().shareDB {
        try {
            require(this.Uid.isNotEmpty()) {"user's uid is empty, can not be insert, please check you logic."}
            val values = this.ToContentValues()
            it.UnderlyingDB.insertOrThrow(table.TableNameIn(it), null, values)
            return@shareDB null
        } catch (e: Exception) {
            return@shareDB Error(e)
        }
    }
}

suspend fun Array<User>.InsertOrUpdate(columns: List<ColumnInfo> = listOf(User.Pubkey, User.Avatar128, User.Avatar640, User.NameEn, User.Name)):Array<String> {
    val failed = this.insert()
    return this.filter { failed.contains(it.Uid) }.toTypedArray().update(columns)
}

suspend fun Array<User>.insert(): Array<String> {
    return getUs().shareDB {
        val failedList: MutableList<String> = mutableListOf()
        it.UnderlyingDB.beginTransaction()
        try {
            for (data in this) {
                try {
                    it.UnderlyingDB.insertOrThrow(table.TableNameIn(it), null, data.ToContentValues())
                } catch (e: Exception) {
                    failedList.add(data.Uid)
                }
            }
            it.UnderlyingDB.setTransactionSuccessful()
        } finally {
            it.UnderlyingDB.endTransaction()
        }
        return@shareDB failedList.toTypedArray()
    }
}

suspend fun Array<User>.update(columns: List<ColumnInfo> = listOf(User.Pubkey, User.Avatar128, User.Avatar640, User.NameEn, User.Name)): Array<String> {
    val failed: MutableList<String> = mutableListOf()
    for (entity in this) {
        if (entity.update(columns) != null) {
            failed.add(entity.Uid)
        }
    }
    return failed.toTypedArray()
}

suspend fun User.update(columns: List<ColumnInfo> = listOf(User.Pubkey, User.Avatar128, User.Avatar640, User.NameEn, User.Name)): Error? {
    return getUs().shareDB {
        val where = User.Uid.eq(this.Uid)
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
