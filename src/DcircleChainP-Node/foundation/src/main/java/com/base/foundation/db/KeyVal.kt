package com.base.foundation.db

import com.base.foundation.getUs
import com.base.foundation.nc.NcEvent
import com.github.xpwu.ktdbtable.DB
import com.github.xpwu.ktdbtable.eq
import com.github.xpwu.ktdbtable.invoke
import com.github.xpwu.ktdbtble.annotation.Column
import com.github.xpwu.ktdbtble.annotation.PrimaryKey
import com.github.xpwu.ktdbtble.annotation.Table
import com.tencent.wcdb.database.SQLiteDatabase

private val table = KeyVal.Companion
@Table("KeyVal")
class KeyVal {
    enum class Keys {
        AliyunSTSToken,
        DIDLastStateTime,
        NodeConfig
    }

    class ChangedEvent(var key:String = "") : NcEvent<String>(listOf(key)) {
        override fun getName(): String {
            return super.getName() + key
        }
    }

    @Column("key", primaryKey = PrimaryKey.ONLY_ONE)
    var Key: String = ""

    @Column("value")
    var Value: String = ""

    @Column("expireTime")
    var ExpireTime: Long = 0

    companion object
}

fun KeyVal.Companion.buildKeyForUid(uid: String):String{
    return "Uid${uid}"
}

suspend fun KeyVal.insert(): Error? {
     return getUs().selfDB {
         return@selfDB Insert(it)
    }
}

fun KeyVal.Insert(it: DB<SQLiteDatabase>): Error? {
    return try {
        val values = this.ToContentValues()
        it.UnderlyingDB.insertOrThrow(table.TableNameIn(it), null, values)
        null
    } catch (e: Exception) {
        Error(e)
    }
}

suspend fun KeyVal.update(): Error? {
    return getUs().selfDB {
        return@selfDB update(it)
    }
}

fun KeyVal.update(it:DB<SQLiteDatabase>): Error? {
    val columns = listOf(KeyVal.Value, KeyVal.ExpireTime)
    val where = KeyVal.Key.eq(this.Key)
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
            return Error("not found")
        }
        return null
    } catch (e: Exception) {
        return Error(e)
    }
}

fun KeyVal.Delete(it:DB<SQLiteDatabase>): Error? {
    val where = KeyVal.Key.eq(this.Key)
    val name = table.TableNameIn(it)
    try {
        val colNum = it.UnderlyingDB.delete(
            name,
            where.ArgSQL,
            where.BindArgs,
        )
        if (colNum <= 0) {
            return Error("not found")
        }
        return null
    } catch (e: Exception) {
        return Error(e)
    }
}

suspend fun Array<KeyVal>.insert(): Array<String> {
    return getUs().selfDB {
        val failedList: MutableList<String> = mutableListOf()
        val db = it.UnderlyingDB
        db.beginTransaction()
        try {
            for (data in this) {
                try {
                    db.insertOrThrow(table.TableNameIn(it), null, data.ToContentValues())
                } catch (e: Exception) {
                    failedList.add(data.Key)
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return@selfDB failedList.toTypedArray()
    }

}

fun KeyVal.Companion.FindByKey(it:DB<SQLiteDatabase>, key: String): KeyVal? {
    val where = KeyVal.Key.eq(key)
    val cursor = it.UnderlyingDB.query(
        table.TableNameIn(it),
        KeyVal.AllColumns().map { it.name }.toTypedArray(),
        where.ArgSQL,
        where.BindArgs,
        null,
        null,
        null
    )
    if (!cursor.moveToFirst()) {
        cursor.close()
        return null
    }
    val doc = KeyVal()
    cursor.ToKeyVal(doc)
    cursor.close()
    return doc
}

suspend fun KeyVal.Companion.FindByKey(key: String): KeyVal? {
    return getUs().selfDB {
       return@selfDB FindByKey(it, key)
    }
}

suspend fun KeyVal.Companion.Set(it:DB<SQLiteDatabase>, key:String, value:String, expireTime:Long=0L):Error? {
    val keyVal = KeyVal()
    keyVal.Key = key
    keyVal.Value = value
    keyVal.ExpireTime = expireTime
    if (keyVal.Insert(it)!=null) {
        keyVal.update(it)
    }

    getUs().nc.postToMain(KeyVal.ChangedEvent(key))
    return null
}

fun KeyVal.Companion.Delete(it:DB<SQLiteDatabase>, key:String) {
    val keyVal = KeyVal()
    keyVal.Key = key
    keyVal.Delete(it)
}
