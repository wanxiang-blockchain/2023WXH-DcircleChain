package com.base.foundation.db

import com.base.foundation.getUs
import com.github.xpwu.ktdbtable.eq
import com.github.xpwu.ktdbtable.invoke
import com.github.xpwu.ktdbtable.lt
import com.github.xpwu.ktdbtable.where.and
import com.github.xpwu.ktdbtble.annotation.Column
import com.github.xpwu.ktdbtble.annotation.PrimaryKey

private val table = Syncer.Companion
@com.github.xpwu.ktdbtble.annotation.Table("Syncer")
class Syncer{
    companion object;

    @Column("key", primaryKey = PrimaryKey.ONLY_ONE)
    var key: String = ""

    @Column("value")
    var value: Int = 0
}

suspend fun Syncer.SetValue(): Error? {
    return getUs().selfDB {
        val columns = listOf(Syncer.value)
        val where = Syncer.key.eq(this.key) and Syncer.value.lt(this.value)
        try {
            val rowId = it.UnderlyingDB.update(
                table.TableNameIn(it),
                this.ToContentValues(columns),
                where.ArgSQL,
                where.BindArgs
            )
            if (rowId > 0) {
                return@selfDB null
            }
        } catch (_: Exception) { }

        try {
            val values = this.ToContentValues()
            val rowId = it.UnderlyingDB.insertOrThrow(table.TableNameIn(it), null, values)
            if (rowId > 0) {
                return@selfDB Error("")
            }
            return@selfDB null
        } catch (e: Exception) {
            return@selfDB Error(e.localizedMessage)
        }
    }
}

suspend fun Syncer.Companion.Get(key: String): Int {
    return getUs().selfDB { it ->
        val where = Syncer.key.eq(key)
        val cursor =
            it.UnderlyingDB.query(
                table.TableNameIn(it),
                Syncer.AllColumns().map { it.name }.toTypedArray(),
                where.ArgSQL,
                where.BindArgs,
                null,
                null,
                null
            )
        if (!cursor.moveToFirst()) {
            cursor.close()
            return@selfDB 0
        }
        val value = cursor.getInt(cursor.getColumnIndexOrThrow(Syncer.value.name))
        cursor.close()
        return@selfDB value
    }
}

suspend fun Syncer.Companion.Set(key:String, value:Int): Error? {
    val syncer = Syncer()
    syncer.key = key
    syncer.value = value
    return syncer.SetValue()
}