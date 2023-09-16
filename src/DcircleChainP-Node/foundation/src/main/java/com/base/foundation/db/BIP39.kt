package com.base.foundation.db

import android.content.res.AssetManager
import com.base.foundation.getAppContext
import com.base.foundation.getUs
import com.github.xpwu.ktdbtable.eq
import com.github.xpwu.ktdbtable.invoke
import com.github.xpwu.ktdbtble.annotation.Column
import com.github.xpwu.ktdbtble.annotation.Index
import com.github.xpwu.ktdbtble.annotation.Table
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

private val table = BIP39.Companion


@Table("BIP39")
open class BIP39 {
    @Index(true, "language_name")
    @Column("language")
    var language: String = ""

    @Column("word")
    @Index(true, "language_name", false, 1)
    @Index
    var word: String = ""

    @Column("length")
    var length: Int = 0

    enum class Language(var value: String) {
        English("english")
    }

    companion object
}


suspend fun Array<BIP39>.insert(): Array<String> {
    return getUs().shareDB.invoke {
        val failedList: MutableList<String> = mutableListOf()
        val name = table.TableNameIn(it)
        it.UnderlyingDB.beginTransaction()
        try {
            for (data in this) {
                try {
                    it.UnderlyingDB.insertOrThrow(name, null, data.ToContentValues())
                } catch (e: Exception) {
                    failedList.add(data.word)
                }
            }
            it.UnderlyingDB.setTransactionSuccessful()
        } finally {
            it.UnderlyingDB.endTransaction()
        }
        return@invoke failedList.toTypedArray()
    }
}

suspend fun BIP39.Companion.GetAllCount(): Int {
    return getUs().shareDB {
        val name = table.TableNameIn(it)

        val query = "Select count(${BIP39.word}) From $name"
        val cursor = it.UnderlyingDB.rawQuery(query, null)
        if (!cursor.moveToFirst()) {
            cursor.close()
            return@shareDB 0
        }
        try {
            val count = cursor.getInt(0)
            cursor.close()
            return@shareDB count
        } catch (e: Error) {
            return@shareDB 0
        }
    }
}

suspend fun BIP39.Companion.Init() {
    val all = BIP39.GetAllCount()
    if (all == 2048) {
        return
    }
    val assetManager: AssetManager = getAppContext().assets
    val inputStream: InputStream = assetManager.open("english.txt")
    val reader = BufferedReader(InputStreamReader(inputStream))
    var line: String?
    val pool = mutableListOf<BIP39>()
    while (reader.readLine().also { line = it } != null) {
        val bip = BIP39()
        bip.word = line.toString()
        bip.language = BIP39.Language.English.value
        bip.length = bip.word.length
        pool.add(bip)
    }
    inputStream.close()
    pool.toTypedArray().insert()
}

suspend fun BIP39.Companion.FindByLanguage(language: BIP39.Language): Array<BIP39> {
    return getUs().shareDB { it ->
        val columns = BIP39.AllColumns().map { it.name }.toTypedArray()
        val name = table.TableNameIn(it)
        val where = BIP39.language.eq(language.value)
        val cursor =
            it.UnderlyingDB.query(
                name,
                columns,
                where.ArgSQL,
                where.BindArgs,
                null,
                null,
                "${BIP39.word} AES"
            )
        val result = mutableListOf<BIP39>()
        if (!cursor.moveToFirst()) {
            cursor.close()
            return@shareDB result.toTypedArray()
        }
        do {
            val doc = BIP39()
            cursor.ToBIP39(doc)
            result.add(doc)
        } while (cursor.moveToNext())
        cursor.close()
        return@shareDB result.toTypedArray()
    }
}

