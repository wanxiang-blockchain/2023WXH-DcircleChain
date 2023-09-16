package com.base.foundation.db

import android.content.ContentValues
import com.base.foundation.getUs
import com.base.foundation.nc.NcEvent
import com.github.xpwu.ktdbtable.ColumnInfo
import com.github.xpwu.ktdbtable.eq
import com.github.xpwu.ktdbtable.gt
import com.github.xpwu.ktdbtable.`in`
import com.github.xpwu.ktdbtable.invoke
import com.github.xpwu.ktdbtable.lt
import com.github.xpwu.ktdbtable.where.and
import com.github.xpwu.ktdbtable.where.or
import com.github.xpwu.ktdbtble.annotation.Column
import com.github.xpwu.ktdbtble.annotation.Index
import com.github.xpwu.ktdbtble.annotation.PrimaryKey
import com.github.xpwu.ktdbtble.annotation.Table
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tencent.wcdb.database.SQLiteDatabase

private val table = DIDArticle.Companion

@Table("DIDArticle")
class DIDArticle {
	class ChangedEvent(var address:String="") : NcEvent<String>(listOf(address)) {
		override fun getName(): String {
			return super.getName() + address
		}

		var editing:Boolean = false
	}


	class AddSecretKeySuccessEvent(var address:String="") : NcEvent<String>(listOf(address)) {
		override fun getName(): String {
			return super.getName() + address
		}
	}


	class MeCreateChangeEvent(address: List<String> = listOf()) : NcEvent<String>(address)

    enum class EStatus(val int: Int) {
		None(0),
		Editing(0x01),
		Uploading(0x10),
		UploadFail(0x15),
		UploadOk(0x20),
		Confirming(0x30),
		ConfirmFail(0x40),
		ConfirmOk(0x50),
		WaitAbstract(0x60),
		WaitToken(0x70),
		Done(0xA0),
		Delete(0xFF);

		companion object
    }

	enum class EPayStatus(val int: Int) {
		None(0x00),
		Paid(0x02),
	}

	companion object {
		var Visible = 0x01
		var InVisible = 0x00
	}

	@SerializedName("address")
	@Column("address", primaryKey = PrimaryKey.ONLY_ONE)
	var Address: String = ""

	@SerializedName("currentBlockRootHash")
	@Column("CurrentBlockRootHash")
	var CurrentBlockRootHash: String = ""

	@SerializedName("creatorUid")
	@Column("creatorUid")
	@Index
	var CreatorUid: String = ""

	@SerializedName("secretKey")
	@Column("secretKey")
	var SecretKey: String = ""

	@SerializedName("createTime")
	@Column("CreateTime")
	var CreateTime: Long = 0

	// Version(1.1)
	@SerializedName("updateTime")
	@Column("UpdateTime")
	var UpdateTime: Long = 0

	@Expose(serialize = false)
	@Column("PayTime")
	var PayTime:Long = 0

	@SerializedName("status")
	@Column("Status")
	var Status: Int = 0

    @Column("PayStatus")
	var PayStatus: Int = 0

    @SerializedName("visible")
	@Column("Visible")
	var visible:Int = 0
	
	@Column("usedVersion")
	var usedVersion:Int = 0

	@Column("editingDevice")
	var editingDevice:String = ""

	/*******************以下数据是当前设备数据**************************/
	@Column("GenesisBlockRootHash")
	var GenesisBlockRootHash:String=""

	@Column("Title")
	var Title: String = ""

	/**
	 * 1、SaveDraft 时默认为空，产生新的块时，则会更新
	 */
	@SerializedName("editingBlockRootHash")
	@Column("EditingBlockRootHash")
	var EditingBlockRootHash: String = ""

	@Column("Context")
	var Context: String = ""
}

fun DIDArticle.EStatus.Companion.valueOf(value: Int): DIDArticle.EStatus? {
	for (item in DIDArticle.EStatus.values()) {
		if (item.int == value) {
			return item
		}
	}
	return null
}


suspend fun Array<DIDArticle>.update(columns:MutableList<ColumnInfo> = mutableListOf(DIDArticle.CurrentBlockRootHash, DIDArticle.visible)): Array<String> {
	val failed: MutableList<String> = mutableListOf()
    for (item in this) {
		if (item.update(columns) != null) {
			failed.add(item.Address)
		}
	}
	return failed.toTypedArray()
}

suspend fun DIDArticle.update(columns:MutableList<ColumnInfo> = mutableListOf(DIDArticle.CurrentBlockRootHash, DIDArticle.visible)): Error? {
	return getUs().selfDB.invoke {
		val where = DIDArticle.Address.eq(this.Address)
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
				return@invoke Error("not found")
			}
			return@invoke null
		} catch (e: Exception) {
			return@invoke Error(e)
        }
	}
}

suspend fun DIDArticle.insert(): Error? {
	return getUs().selfDB.invoke {
		val values = this.ToContentValues()
		val name = table.TableNameIn(it)
		try {
			it.UnderlyingDB.insertOrThrow(name, null, values)
			return@invoke null
        } catch (e: Exception) {
			return@invoke Error(e)
		}
	}
}

suspend fun Array<DIDArticle>.insert(): Array<String> {
	return getUs().selfDB.invoke {
		val failedList: MutableList<String> = mutableListOf()
		val name = table.TableNameIn(it)
		it.UnderlyingDB.beginTransaction()
		try {
			for (data in this) {
				try {
					it.UnderlyingDB.insertOrThrow(name, null, data.ToContentValues())
				} catch (e: Exception) {
					failedList.add(data.Address)
				}
			}
			it.UnderlyingDB.setTransactionSuccessful()
		} finally {
			it.UnderlyingDB.endTransaction()
		}
		return@invoke failedList.toTypedArray()
	}
}

suspend fun DIDArticle.Companion.findByAddress(address: String): DIDArticle? {
	return getUs().selfDB { it ->
        val where = DIDArticle.Address.eq(address)
		val cursor =
			it.UnderlyingDB.query(
				table.TableNameIn(it),
				DIDArticle.AllColumns().map { it.name }.toTypedArray(),
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
		val doc = DIDArticle()
        cursor.ToDIDArticle(doc)
        cursor.close()
        return@selfDB doc
    }
}

suspend fun DIDArticle.Companion.findByAddress(address:Array<String>): MutableList<DIDArticle> {
	return getUs().selfDB { it ->
        val columns = DIDArticle.AllColumns().map { it.name }.toTypedArray()
		val name = table.TableNameIn(it)
		val pageSize = 999
		val resultList = mutableListOf<DIDArticle>()

		for (startIndex in address.indices step pageSize){
			val endIndex = minOf(startIndex + pageSize, address.size)
			val addressPage = address.sliceArray(startIndex until endIndex)

			val where = DIDArticle.Address.`in`(addressPage)
			val cursor =
				it.UnderlyingDB.query(
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
                return@selfDB resultList
            }


			do {
				val doc = DIDArticle()
                cursor.ToDIDArticle(doc)
                resultList.add(doc)
			} while (cursor.moveToNext())

			cursor.close()

        }

		return@selfDB resultList
    }
}
suspend fun DIDArticle.Companion.FindByCreatorUid(creatorUid:String): MutableList<DIDArticle> {
	return getUs().selfDB { it ->
        val where = DIDArticle.CreatorUid.eq(creatorUid) and DIDArticle.Status.eq(DIDArticle.EStatus.Done.int) and DIDArticle.visible.eq(
            Visible
        )
		val cursor = it.UnderlyingDB.query(
				table.TableNameIn(it),
				DIDArticle.AllColumns().map { it.name }.toTypedArray(),
				where.ArgSQL,
				where.BindArgs,
				null,
				null,
				"${DIDArticle.UpdateTime.name} DESC"
			)

		val list = mutableListOf<DIDArticle>()

		DIDArticle.FindCurrentProcess()?.apply {
			list.add(this)
		}

		if (!cursor.moveToFirst()) {
			cursor.close()
            return@selfDB list
        }


		do {
			val doc = DIDArticle()
            cursor.ToDIDArticle(doc)
            list.add(doc)
		} while (cursor.moveToNext())

		cursor.close()
        return@selfDB list
    }
}

suspend fun DIDArticle.Companion.FindCurrentProcess(): DIDArticle? {
	return getUs().selfDB { it ->
        val where = ((DIDArticle.Status.gt(DIDArticle.EStatus.Editing.int) and DIDArticle.Status.lt(DIDArticle.EStatus.Done.int)) or (DIDArticle.Status.eq(DIDArticle.EStatus.Done.int) and DIDArticle.visible.eq(
            InVisible
        ))) and DIDArticle.CreatorUid.eq(getUs().getUid())
		val cursor =
			it.UnderlyingDB.query(
				table.TableNameIn(it),
				DIDArticle.AllColumns().map { it.name }.toTypedArray(),
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
		val doc = DIDArticle()
        cursor.ToDIDArticle(doc)
        cursor.close()
        return@selfDB doc
    }
}


suspend fun DIDArticle.Companion.SetStateTo(address: String, oldState:Int, newState:Int): Error? {
	return getUs().selfDB.invoke {
		val values = ContentValues().apply {
			this.put(DIDArticle.Status.name, newState)
		}
		val where = DIDArticle.Address.eq(address) and DIDArticle.Status.eq(oldState)
		val name = table.TableNameIn(it)
		try {
			val rowId = it.UnderlyingDB.update(name, values, where.ArgSQL, where.BindArgs)
			if (rowId<=0) {
				return@invoke Error("not found")
			}

			return@invoke null
		} catch (e: Exception) {
			return@invoke Error(e)
        }
	}
}

suspend fun DIDArticle.Companion.Delete(address:String): Int {
	return getUs().selfDB {
		val where = DIDArticle.Address.eq(address)
		return@selfDB it.UnderlyingDB.delete(table.TableNameIn(it), where.ArgSQL, where.BindArgs)
	}
}

suspend fun GetDIDArticleARMeta(article:DIDArticle): DIDBlockMeta? {
	// FIX: 不知道什么原因，多端时，EditingBlockRootHash 未被清除
	if (article.Status < DIDArticle.EStatus.ConfirmOk.int) {
		DIDArticleBlock.FindByRootHash(article.EditingBlockRootHash)?.apply {
			return DIDBlockMeta.FindByRootHash(this.ARMetaRootHash)
		}
	}


	DIDArticleBlock.FindByRootHash(article.CurrentBlockRootHash)?.apply {
		return DIDBlockMeta.FindByRootHash(this.ARMetaRootHash)
	}

	return null

}

suspend fun GetDIDArticleEncMeta(article:DIDArticle): DIDBlockMeta? {
	// FIX: 不知道什么原因，多端时，EditingBlockRootHash 未被清除
	if (article.Status < DIDArticle.EStatus.ConfirmOk.int) {
		DIDArticleBlock.FindByRootHash(article.EditingBlockRootHash)?.apply {
			return DIDBlockMeta.FindByRootHash(this.EncRootHash)
		}
	}


	DIDArticleBlock.FindByRootHash(article.CurrentBlockRootHash)?.apply {
		return DIDBlockMeta.FindByRootHash(this.EncRootHash)
	}

	return null
}
