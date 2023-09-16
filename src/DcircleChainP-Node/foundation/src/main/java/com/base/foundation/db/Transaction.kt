package com.base.foundation.db

import com.base.foundation.getUs
import com.github.xpwu.ktdbtable.invoke
import com.github.xpwu.ktdbtble.annotation.Column
import com.github.xpwu.ktdbtble.annotation.Index
import com.github.xpwu.ktdbtble.annotation.PrimaryKey
import com.github.xpwu.ktdbtble.annotation.Table

private val table = Transaction.Companion

@Table("TxID")
class Transaction {
    companion object;

    @Column("txId", primaryKey = PrimaryKey.ONLY_ONE)
    var TxId: String = ""

    @Column("opCode")
    @Index(true, "opcode", sequence = 0)
    var OpCode: Int = 0

    @Column("_to")
    @Index(true, "_to", sequence = 1)
    var To: String = ""
}

suspend fun Transaction.insert(): Error? {
    return getUs().selfDB {
        try {
            val name = table.TableNameIn(it)
            val values = this.ToContentValues()
            it.UnderlyingDB.insertOrThrow(name, null, values)
            return@selfDB null
        } catch (e: Exception) {
            return@selfDB Error(e)
        }
    }
}