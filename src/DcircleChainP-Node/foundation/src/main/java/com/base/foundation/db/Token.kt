package com.base.foundation.db

import com.base.foundation.getUs
import com.blankj.utilcode.util.LogUtils
import com.github.xpwu.ktdbtable.eq
import com.github.xpwu.ktdbtable.invoke
import com.github.xpwu.ktdbtble.annotation.Column
import com.github.xpwu.ktdbtble.annotation.PrimaryKey

private val table = Token.Companion
@com.github.xpwu.ktdbtble.annotation.Table("Token")
class Token {
    companion object {
        const val empty = ""
    }

    @Column("id", primaryKey = PrimaryKey.ONLY_ONE)
    var id: String = ""

    @Column("token")
    var token: String = ""
}


suspend fun Token.Companion.getValue(id: String): String {
    return getUs().selfDB { it ->
        val where = Token.id.eq(id)
        val cursor = it.UnderlyingDB.query(
                table.TableNameIn(it),
                Token.AllColumns().map { it.name }.toTypedArray(),
                where.ArgSQL,
                where.BindArgs,
                null,
                null,
                null
            )
        if (!cursor.moveToFirst()) {
            cursor.close()
            return@selfDB empty
        }
        val token = cursor.getString(cursor.getColumnIndexOrThrow(Token.token.name))
        cursor.close()
        return@selfDB token
    }
}

suspend fun Token.setValue(): Error? {
    if (this.id.isEmpty()) {
        throw Error("Token.setValue is is empty")
    }

    return getUs().selfDB {
        try {
            val values = this.ToContentValues()
            val rowId = it.UnderlyingDB.insertOrThrow(table.TableNameIn(it), null, values)
            LogUtils.d("Token", "setValue", "rowId=${rowId}")
            return@selfDB null
        } catch (e: Exception) {
            val columns = listOf(Token.token)
            val where = Token.id.eq(this.id)
            try {
                val colNum = it.UnderlyingDB.updateWithOnConflict(
                    table.TableNameIn(it),
                    this.ToContentValues(columns),
                    where.ArgSQL,
                    where.BindArgs,
                    com.tencent.wcdb.database.SQLiteDatabase.CONFLICT_IGNORE
                )
                if (colNum <= 0) {
                    return@selfDB Error("not found")
                }
                return@selfDB null
            } catch (e: Exception) {
                return@selfDB Error(e)
            }
        }
    }
}

suspend fun Token.clear(): Array<String> {
    if (this.id.isEmpty()) {
        throw Error("Token.clear is is empty")
    }

    return getUs().selfDB.invoke {
        val failed: MutableList<String> = mutableListOf()
        val where = Token.id.eq(this.id)
        try {
            it.UnderlyingDB.delete(table.TableNameIn(it), where.ArgSQL, where.BindArgs)
        } catch (e: Exception) {
            failed.add(this.id)
        }
        return@invoke failed.toTypedArray()
    }
}
