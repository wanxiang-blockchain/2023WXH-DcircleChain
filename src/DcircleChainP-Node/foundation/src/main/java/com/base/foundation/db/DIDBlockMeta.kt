package com.base.foundation.db

import com.base.foundation.getUs
import com.base.foundation.nc.NcEvent
import com.base.thridpart.toHexString
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.github.xpwu.ktdbtable.eq
import com.github.xpwu.ktdbtable.`in`
import com.github.xpwu.ktdbtable.invoke
import com.github.xpwu.ktdbtble.annotation.*
import com.github.xpwu.ktdbtble.annotation.Table
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.tencent.wcdb.database.SQLiteDatabase
import wallet.core.jni.Hash

private val table = DIDBlockMeta.Companion

open class DIDBlockMetaNode {
	var hash:String = ""
	var type:Int = 0

	enum class Type(var int: Int) {
		Unknown(404),
		Text(1),
		Image(2),
		Article(3),
	}
}

class DIDBlockMetaTextNode() : DIDBlockMetaNode() {
	init {
		this.type = Type.Text.int
	}

	constructor(text:String) : this() {
		this.text = text
        this.hash = Hash.keccak256(text.toByteArray(Charsets.UTF_8)).toHexString()
    }

	var text:String = ""
}

open class DIDBlockMetaContentNode : DIDBlockMetaNode(),MultiItemEntity {
	class DIDBlockMetaArticleNode {
		var didAddress:String = ""
		var title:String? = null
		var abstractText:String? = null
		var abstractImages: List<ImageAttachment>? = null
	}

	class ImageAttachment{
		var objectId:String? = null
		var key :String? = null
		var height:Int? = null
		var width:Int? = null
	}

	init {
		this.type = Type.Image.int
	}

	var objectKey:String = ""
	var objectId:String = ""

	enum class Version(var int:Int) {
		Mobilenet_V2_140_224_tflite(1)
	}

	// ---------------图片特征字段----start-----------------------
	data class Feature(val feature:String, val x:Int, val y:Int, val w:Int, val h:Int)

    var version:Int? = null  							 // 特征文件版本，V1 开始有
	var feature:String? = null						 // 特征文件 CID，V1 开始有
	var feature1x:MutableList<Feature> = mutableListOf()	// 特征 224x224
	var feature1xFull:String?=null 				 // feature1x 特征文件全文件 |Version | hash | feature|
	// --------------图片特征字段---- end  -----------------------

	var suffix:String = ""
	var width:Int = 0
	var height:Int = 0
	var ref:DIDBlockMetaArticleNode? = null
	override val itemType: Int
		get() = type
}

@FromByteArray
fun DIDBlockMetaImageNode_FromByteArray2(byteArray: ByteArray):Array<DIDBlockMetaContentNode> {
	return Gson().fromJson(String(byteArray, Charsets.UTF_8), Array<DIDBlockMetaContentNode>::class.java)
}

@ToByteArray
fun DIDBlockMetaImageNode_ToByteArray2(node:Array<DIDBlockMetaContentNode>):ByteArray {
	return Gson().toJson(node).toByteArray(Charsets.UTF_8)
}

@FromByteArray
fun DIDBlockMetaTextNode_FromByteArray(byteArray: ByteArray):DIDBlockMetaTextNode {
	return Gson().fromJson(String(byteArray, Charsets.UTF_8), DIDBlockMetaTextNode::class.java)
}


@ToByteArray
fun DIDBlockMetaTextNode_ToByteArray(node:DIDBlockMetaTextNode):ByteArray {
	return Gson().toJson(node).toByteArray(Charsets.UTF_8)
}


@Table("DIDBlockMeta")
class DIDBlockMeta {
	class ChangedEvent(private var rootHash: String="") : NcEvent<String>(listOf(rootHash)) {
		override fun getName(): String {
			return super.getName() + rootHash
		}
	}

	companion object {
		const val AlgorithmNTree = 1
	}

	@SerializedName("rootHash")
	@Column("rootHash", primaryKey = PrimaryKey.ONLY_ONE)
	var RootHash: String = ""

	@SerializedName("algorithm")
	@Column("Algorithm")
	var Algorithm: Int = 0

	@SerializedName("title")
	@Column("Title")
	var Title: DIDBlockMetaTextNode = DIDBlockMetaTextNode()

	@SerializedName("content")
	@Column("Content")
	var Content: Array<DIDBlockMetaContentNode> = arrayOf()
}

private fun DIDBlockMeta.Companion.getNTreeRootHash(titleHas:String,leaves: Array<DIDBlockMetaContentNode>):String {
	val data = titleHas + leaves.joinToString("") { it.hash }
	return Hash.keccak256(data.toByteArray(Charsets.UTF_8)).toHexString()
}

suspend fun DIDBlockMeta.insert(): Error? {
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

suspend fun DIDBlockMeta.update(): Error? {
	return getUs().selfDB.invoke {
		val columns = listOf(
			DIDBlockMeta.RootHash,
			DIDBlockMeta.Title,
			DIDBlockMeta.Content
		)

		val where = DIDBlockMeta.RootHash.eq(this.RootHash)
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
			LogUtils.e("DIDArticle.Update err", e)
			return@invoke Error(e)
        }
	}
}


suspend fun DIDBlockMeta.Companion.FindByRootHash(rootHash: String): DIDBlockMeta? {
	return getUs().selfDB { it ->
		val where = DIDBlockMeta.RootHash.eq(rootHash)
		val cursor =
			it.UnderlyingDB.query(
				table.TableNameIn(it),
				DIDBlockMeta.AllColumns().map { it.name }.toTypedArray(),
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
		val doc = DIDBlockMeta()
        cursor.ToDIDBlockMeta(doc)
        cursor.close()
        return@selfDB doc
    }
}

suspend fun Array<DIDBlockMeta>.insert(): Array<String> {
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

suspend fun DIDBlockMeta.Companion.FindByRootHash(rootHash: Array<String>): Array<DIDBlockMeta> {
	return getUs().selfDB { it ->
		val columns = DIDBlockMeta.AllColumns().map { it.name }.toTypedArray()
		val name = table.TableNameIn(it)

		val pageSize = 999
		val result = mutableListOf<DIDBlockMeta>()

		for (startIndex in rootHash.indices step pageSize) {
			val endIndex = minOf(startIndex + pageSize, rootHash.size)
			val rootHashPage = rootHash.sliceArray(startIndex until endIndex)
			val where = DIDBlockMeta.RootHash.`in`(rootHashPage)
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
                return@selfDB result.toTypedArray()
            }
			do {
				val doc = DIDBlockMeta()
                cursor.ToDIDBlockMeta(doc)
                result.add(doc)
			} while (cursor.moveToNext())
			cursor.close()
        }

		return@selfDB result.toTypedArray()
    }
}