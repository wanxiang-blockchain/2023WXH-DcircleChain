package com.base.foundation.db

import com.base.foundation.getUs
import com.base.foundation.nc.NcEvent
import com.blankj.utilcode.util.LogUtils
import com.github.xpwu.ktdbtable.ColumnInfo
import com.github.xpwu.ktdbtable.eq
import com.github.xpwu.ktdbtable.invoke
import com.github.xpwu.ktdbtble.annotation.*
import com.github.xpwu.ktdbtble.annotation.Table
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

private val table = DidArticleTag.Companion

open class ArticleTag {
    var address:String = ""
}
@FromByteArray
fun ArticleTag_FromByteArray2(byteArray: ByteArray):Array<ArticleTag> {
    return Gson().fromJson(String(byteArray, Charsets.UTF_8), Array<ArticleTag>::class.java)
}

@ToByteArray
fun ArticleTag_ToByteArray2(node:Array<ArticleTag>):ByteArray {
    return Gson().toJson(node).toByteArray(Charsets.UTF_8)
}

@Table("DidArticleTag")
class DidArticleTag {
    class ChangedEvent(var id:String = "") : NcEvent<String>(listOf(id)) {
        override fun getName(): String {
            return super.getName() + id
        }
    }

    companion object;

    @Column("id", primaryKey = PrimaryKey.ONLY_ONE)
    var id: String = ""

    @SerializedName("chatId")
    @Column("chatId")
    var chatId: String = ""

    @SerializedName("tagId")
    @Column("tagId")
    var tagId: String = ""

    @SerializedName("version")
    @Column("version")
    var version: Int = 0

    @SerializedName("article")
    @Column("article")
    var article: Array<ArticleTag> = arrayOf()
}

suspend fun DidArticleTag.insert(): Error? {
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


suspend fun DidArticleTag.update(columns: List<ColumnInfo> = listOf(DidArticleTag.version, DidArticleTag.article)): Error? {
    return getUs().selfDB{
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
            LogUtils.w("Community.Update err", e.toString())
            return@selfDB Error(e)
        }
    }
}

