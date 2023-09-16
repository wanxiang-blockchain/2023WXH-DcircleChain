package com.base.foundation.db

import com.base.foundation.getUs
import com.base.foundation.nc.NcEvent
import com.base.thridpart.toJson
import com.blankj.utilcode.util.LogUtils
import com.github.xpwu.ktdbtable.eq
import com.github.xpwu.ktdbtable.`in`
import com.github.xpwu.ktdbtable.invoke
import com.github.xpwu.ktdbtble.annotation.Column
import com.github.xpwu.ktdbtble.annotation.PrimaryKey
import com.github.xpwu.ktdbtble.annotation.Table
import com.google.gson.annotations.SerializedName

private val table = DIDArticleBlock.Companion

@Table("DIDArticleBlock")
class DIDArticleBlock {
	class ChangedEvent(private var rootHash: String = "") : NcEvent<String>(listOf(rootHash)) {
		override fun getName(): String {
			return super.getName() + rootHash
		}
	}

	companion object;

    @SerializedName("rootHash")
	@Column("RootHash", primaryKey = PrimaryKey.ONLY_ONE)
	var RootHash: String = ""

	@SerializedName("preRootHash")
	@Column("PreRootHash")
	var PreRootHash: String = ""

	@SerializedName("crMetaRootHash")
	@Column("CRMetaRootHash")
	var CRMetaRootHash: String = ""

	@SerializedName("arMetaRootHash")
	@Column("ARMetaRootHash")
	var ARMetaRootHash: String = ""

	@SerializedName("featureRootHash")
	@Column("FeatureRootHash")
	var FeatureRootHash: String = ""

	@SerializedName("encRootHash")
	@Column("EncRootHash")
	var EncRootHash: String = ""

	@SerializedName("tokenAddress")
	@Column("TokenAddress")
	var TokenAddress: String = ""

	@SerializedName("version")
	@Column("Version")
	var Version:Int = 0

    @Deprecated("1.1")
	@SerializedName("status")
	@Column("Status")
	var Status:Int = 0

	@SerializedName("address")
	@Column("Address")
	var CreatorAddress:String = ""

	@SerializedName("salt")
	@Column("Salt")
	var Salt:String = ""

	@SerializedName("initCode")
	@Column("InitCode")
	var InitCode:Int = 0

	@SerializedName("didAddress")
	@Column("DIDAddress")
	var DIDAddress:String = ""
}


suspend fun DIDArticleBlock.insert(): Error? {
	return getUs().selfDB.invoke {
		val values = this.ToContentValues()
		val name = table.TableNameIn(it)
		try {
			it.UnderlyingDB.insertOrThrow(name, null, values)
			return@invoke null
        } catch (e: Exception) {
			LogUtils.w("DIDArticleBlock.Insert err", e.toString(), this.toJson())
			return@invoke Error(e)
		}
	}
}

suspend fun Array<DIDArticleBlock>.insert(): Array<String> {
	return getUs().selfDB.invoke {
		val failedList: MutableList<String> = mutableListOf()
		val name = table.TableNameIn(it)
		it.UnderlyingDB.beginTransaction()
		try {
			for (data in this) {
				try {
					it.UnderlyingDB.insertOrThrow(name, null, data.ToContentValues())
				} catch (e: Exception) {
					failedList.add(data.RootHash)
				}
			}
			it.UnderlyingDB.setTransactionSuccessful()
		} finally {
			it.UnderlyingDB.endTransaction()
		}
		return@invoke failedList.toTypedArray()
	}
}

suspend fun DIDArticleBlock.delete():Error? {
	return getUs().selfDB {
		val where = DIDArticleBlock.RootHash.eq(this.RootHash)
		try {
			it.UnderlyingDB.delete(table.TableNameIn(it), where.ArgSQL, where.BindArgs)
		} catch (e: Exception) {
			return@selfDB Error(e.message)
		}

		return@selfDB null
	}
}


suspend fun Array<DIDArticleBlock>.delete(): Array<String> {
	return getUs().selfDB {
		val failed: MutableList<String> = mutableListOf()
        val name = table.TableNameIn(it)
		for (item in this) {
			val where = DIDArticleBlock.RootHash.eq(item.RootHash)
			try {
				it.UnderlyingDB.delete(name, where.ArgSQL, where.BindArgs)
			} catch (e: Exception) {
				failed.add(item.RootHash)
			}
		}
		return@selfDB failed.toTypedArray()
	}
}

suspend fun DIDArticleBlock.Companion.FindByRootHash(rootHash: String): DIDArticleBlock? {
	return getUs().selfDB { it ->
        val where = DIDArticleBlock.RootHash.eq(rootHash)
		val cursor =
			it.UnderlyingDB.query(
				table.TableNameIn(it),
				DIDArticleBlock.AllColumns().map { it.name }.toTypedArray(),
				where.ArgSQL,
				where.BindArgs,
				null,
				null,
				null
			)
		if (!cursor.moveToFirst()) {
			cursor.close()
            return@selfDB null
        }
		val doc = DIDArticleBlock()
        cursor.ToDIDArticleBlock(doc)
        cursor.close()
        return@selfDB doc
    }
}

suspend fun DIDArticleBlock.Companion.FindByRootHash(rootHash: Array<String>): Array<DIDArticleBlock> {
	return getUs().selfDB { it ->
        val columns = DIDArticleBlock.AllColumns().map { it.name }.toTypedArray()
		val name = table.TableNameIn(it)
		val pageSize = 999
		val result = mutableListOf<DIDArticleBlock>()

		for (startIndex in rootHash.indices step pageSize) {
			val endIndex = minOf(startIndex + pageSize, rootHash.size)
			val rootHashPage = rootHash.sliceArray(startIndex until endIndex)

			val where = DIDArticleBlock.RootHash.`in`(rootHashPage)
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
                return@selfDB result.toTypedArray()
            }

			do {
				val doc = DIDArticleBlock()
                cursor.ToDIDArticleBlock(doc)
                result.add(doc)
			} while (cursor.moveToNext())
			cursor.close()
        }

		return@selfDB result.toTypedArray()
    }
}

