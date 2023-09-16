package com.base.foundation.db

import com.base.foundation.getUs
import com.base.foundation.nc.NcEvent
import com.blankj.utilcode.util.LogUtils
import com.github.xpwu.ktdbtable.ColumnInfo
import com.github.xpwu.ktdbtable.eq
import com.github.xpwu.ktdbtable.invoke
import com.github.xpwu.ktdbtble.annotation.*
import com.github.xpwu.ktdbtble.annotation.Table
import com.google.gson.annotations.SerializedName

private val table = DidArticleTransfer.Companion

@Table("DidArticleTransfer")
class DidArticleTransfer {
    class ChangedEvent(ids: List<String>) : NcEvent<String>(ids) {
        constructor() : this(listOf())
    }

    companion object;

    @SerializedName("transferId")
    @Column("id", primaryKey = PrimaryKey.ONLY_ONE)
    var id: String = ""

    @SerializedName("didAddress")
    @Column("didAddress")
    var didAddress: String = ""

    @SerializedName("fromChat")
    @Column("fromChat")
    var fromChat: String = ""

    @SerializedName("toChat")
    @Column("toChat")
    var toChat: String = ""

    @SerializedName("transferTime")
    @Column("transferTime")
    var transferTime: Long = 0
}

suspend fun DidArticleTransfer.insert(): Error? {
    return getUs().selfDB {
        val values = this.ToContentValues()
        val name = table.TableNameIn(it)
        try {
            it.UnderlyingDB.insertOrThrow(name, null, values)
            return@selfDB null
        } catch (e: Exception) {
            return@selfDB Error(e)
        }
    }
}

suspend fun Array<DidArticleTransfer>.insert(): Array<String> {
    return getUs().selfDB {
        val failedList: MutableList<String> = mutableListOf()
        val name = table.TableNameIn(it)
        val db = it.UnderlyingDB
        db.beginTransaction()
        try {
            for (data in this) {
                try {
                    db.insertOrThrow(name, null, data.ToContentValues())
                } catch (e: Exception) {
                    failedList.add(data.id)
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return@selfDB failedList.toTypedArray()
    }
}

suspend fun DidArticleTransfer.update(): Error? {
    return getUs().selfDB{
        val columns: List<ColumnInfo> = listOf(DidArticleTransfer.didAddress, DidArticleTransfer.fromChat, DidArticleTransfer.toChat, DidArticleTransfer.transferTime)
        val values = this.ToContentValues(columns)
        val where = DidArticleTag.id.eq(this.id)
        val name = table.TableNameIn(it)
        try {
            val colNum = it.UnderlyingDB.update(
                name,
                values,
                where.ArgSQL,
                where.BindArgs
            )
            if (colNum <= 0) {
                return@selfDB Error("not found")
            }
            return@selfDB null
        } catch (e: Exception) {
            LogUtils.w("DidArticleTransfer.Update err", e.toString())
            return@selfDB Error(e)
        }
    }
}

