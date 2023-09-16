package com.base.foundation.db

import com.base.foundation.getUs
import com.github.xpwu.ktdbtable.eq
import com.github.xpwu.ktdbtable.invoke
import com.github.xpwu.ktdbtble.annotation.Column
import com.github.xpwu.ktdbtble.annotation.PrimaryKey
import com.tencent.wcdb.database.SQLiteDatabase


private val table = Me.Companion
@com.github.xpwu.ktdbtble.annotation.Table("Me")
class Me{
    companion object {
        const val TAG = "Me"
        var empty = ""
        var me = "me"
    }

    @Column("id", primaryKey = PrimaryKey.ONLY_ONE)
    var id: String = ""

    @Column("uid")
    var uid: String = ""
}

suspend fun Me.Companion.value(id: String): String? {
    return getUs().shareDB { it ->
        val where = Me.id.eq(id)
        val cursor =
            it.UnderlyingDB.query(
                table.TableNameIn(it),
                Me.AllColumns().map { it.name }.toTypedArray(),
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

        val doc = Me()
        cursor.ToMe(doc)
        cursor.close()
        return@shareDB doc.uid
    }
}

suspend fun Me.Companion.setValue(key:String, value:String):Error? {
    val me = Me()
    me.id = key
    me.uid = value
    return me.setValue()
}

suspend fun Me.setValue(): Error? {
    return getUs().shareDB {
        try {
            val values = this.ToContentValues()
            it.UnderlyingDB.insertOrThrow(table.TableNameIn(it), null, values)
            return@shareDB null
        } catch (e: Exception) {
            val columns = listOf(Me.uid)
            val where = Me.id.eq(this.id)
            try {
                val colNum = it.UnderlyingDB.updateWithOnConflict(
                    table.TableNameIn(it),
                    this.ToContentValues(columns),
                    where.ArgSQL,
                    where.BindArgs,
                    SQLiteDatabase.CONFLICT_IGNORE
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
}