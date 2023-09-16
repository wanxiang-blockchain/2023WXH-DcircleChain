package com.base.foundation.db

import android.content.ContentValues
import com.base.foundation.baselib.UserSpace
import com.base.foundation.getUs
import com.base.foundation.nc.NcEvent
import com.base.foundation.oss.Priority
import com.github.xpwu.ktdbtable.eq
import com.github.xpwu.ktdbtable.invoke
import com.github.xpwu.ktdbtable.lt
import com.github.xpwu.ktdbtable.where.and
import com.github.xpwu.ktdbtable.where.or
import com.github.xpwu.ktdbtble.annotation.Column
import com.github.xpwu.ktdbtble.annotation.PrimaryKey
import com.github.xpwu.ktdbtble.annotation.Table
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ConcurrentHashMap

private val table = DownloadTask.Companion
@Table("DownloadTask")
class DownloadTask() {
	enum class Code(var int: Int) {
		OK(200),
		FAIL(500),
		NotFound(404),
		TokenExpire(403),
	}
	constructor(objectId:String, progress: Int, priority: Priority = com.base.foundation.oss.Priority.Channel4) : this() {
		Id = objectId
        Progress = progress
        Priority = priority.value
		FirstJoinTime = Date().time
		LastJoinTime = Date().time
	}
	
	class ProgressEvent(var objectId: String = "") : NcEvent<String>(listOf(objectId)) {
		override fun getName(): String {
			return super.getName() + objectId
		}
	}

	class SuccessEvent(var objectId: String = "") : NcEvent<String>(listOf(objectId)) {
		override fun getName(): String {
			return super.getName() + objectId
		}
	}

	class FailEvent(var objectId: String = "") : NcEvent<String>(listOf(objectId)) {
		override fun getName(): String {
			return super.getName() + objectId
		}
	}

	companion object {
		val cache:ConcurrentHashMap<String, Int> = ConcurrentHashMap()
		val jobs:ConcurrentHashMap<String, Timer> = ConcurrentHashMap()
	}

	@Column("id", primaryKey = PrimaryKey.ONLY_ONE)
	var Id: String = ""

	@Column("progress")
	var Progress: Int = 0

	@Column("LastJoinTime")
	var LastJoinTime:Long = 0

	@Column("FirstJoinTime")
	var FirstJoinTime:Long = 0

	@Column("Priority")
	var Priority:Int = 0

	@Column("Retry")
	var Retry:Int = 0

	@Column("StatusCode")
	var StatusCode:Int = 0

	@Column("LastStartTime")
	var LastStartTime:Long = 0

	@Column("LastEndTime")
	var LastEndTime:Long = 0

	enum class NetworkType(var int: Int) {
		WIFI(0xFF),
		Cellular(0x00),
	}
	@Column("NetworkType")
	var networkType:Int = 0
}

fun DownloadTask.SetProgressDelay(delay:Long=500):Boolean {
	if (DownloadTask.cache[this.Id]!=null && DownloadTask.cache[this.Id]!! >= this.Progress) {
		return false
	}

	DownloadTask.cache[this.Id] = this.Progress

	if (DownloadTask.jobs[this.Id]!=null) {
		DownloadTask.jobs.remove(this.Id)?.cancel()
	}

	val timer = Timer()
    timer.schedule(object : TimerTask() {
		override fun run() {
			CoroutineScope(Dispatchers.IO).launch {
				this@SetProgressDelay.SetProgress()
			}
		}

	}, delay)
	DownloadTask.jobs[this.Id] = timer

    return true
}

suspend fun DownloadTask.SetProgress(overwrite:Boolean = false) {
	if (overwrite) {
		DownloadTask.cache.remove(this.Id)
	}
	return getUs().selfDB {
		val name = table.TableNameIn(it)
		val values = ContentValues()
        values.put(DownloadTask.Id.name, this.Id)
		values.put(DownloadTask.Progress.name, this.Progress)

		var where = DownloadTask.Id.eq(this.Id) and DownloadTask.Progress.lt(this.Progress)
		if (overwrite) {
			where = DownloadTask.Id.eq(this.Id)
		}

		val rowId = it.UnderlyingDB.update(name, values, where.ArgSQL, where.BindArgs)
		if (rowId>0) {
			return@selfDB
		}

		it.UnderlyingDB.insert(name, null, values).toInt()
		return@selfDB
    }
}

suspend fun DownloadTask.SetNetworkType():Error? {
	return getUs().selfDB {
		val name = table.TableNameIn(it)
		val values = ContentValues()
        values.put(DownloadTask.networkType.name, this.networkType)

		val where = DownloadTask.Id.eq(this.Id)
		val rowId = it.UnderlyingDB.update(name, values, where.ArgSQL, where.BindArgs)
		if (rowId>0) {
			return@selfDB null
		}

		return@selfDB Error("not found")
	}
}

suspend fun DownloadTask.delete():Error? {
	return getUs().selfDB {
		val where = DownloadTask.Id.eq(this.Id)
		val name = table.TableNameIn(it)
		val rowId = it.UnderlyingDB.delete(name, where.ArgSQL, where.BindArgs)
		if (rowId>0) {
			return@selfDB null
		}

		return@selfDB Error("not found")
	}
}

suspend fun DownloadTask.SetStatusCode():Error? {
	return getUs().selfDB {
		val name = table.TableNameIn(it)
		val values = ContentValues()
        values.put(DownloadTask.StatusCode.name, this.StatusCode)

		val where = DownloadTask.Id.eq(this.Id)
		val rowId = it.UnderlyingDB.update(name, values, where.ArgSQL, where.BindArgs)
		if (rowId>0) {
			return@selfDB null
		}

		return@selfDB Error("not found")
	}
}

suspend fun DownloadTask.Companion.Delete(us:UserSpace) {
	return us.selfDB.invoke {
		val name = table.TableNameIn(it)
		val where = DownloadTask.Progress.eq(100) or DownloadTask.StatusCode.eq(200)
		it.UnderlyingDB.delete(name, where.ArgSQL, where.BindArgs)
	}
}

suspend fun DownloadTask.Companion.FindDownloadingTask():List<DownloadTask> {
	return getUs().selfDB.invoke { it ->
        val docs = mutableListOf<DownloadTask>()
		val name = table.TableNameIn(it)
		try {
			val columns = DownloadTask.AllColumns().map { it.name }.toTypedArray()
			val where = DownloadTask.Progress.lt(100) and DownloadTask.StatusCode.eq(0)
			val cursor = it.UnderlyingDB.query(
				name,
				columns,
				where.ArgSQL,
				where.BindArgs,
				null,
				null,
				"${DownloadTask.FirstJoinTime.name} DESC"
			)
			if (!cursor.moveToFirst()) {
				cursor.close()
                return@invoke docs.toList()
			}

			do {
				val doc = DownloadTask()
                cursor.ToDownloadTask(doc)
				docs.add(doc)
			} while (cursor.moveToNext())

			cursor.close()
			return@invoke docs.toList()
		} catch (e: Exception) {
			return@invoke docs.toList()
		}
	}
}

suspend fun DownloadTask.Companion.FindByPriority(priority: Priority, excluded:MutableList<String>, networkType: List<DownloadTask.NetworkType>):DownloadTask? {
	return getUs().selfDB { it ->
        val name = table.TableNameIn(it)
		val columns =  DownloadTask.AllColumns().map { it.name }.toTypedArray()
		var where = "SELECT ${columns.joinToString(",")} FROM $name WHERE ${DownloadTask.Progress.name} < 100 AND ${DownloadTask.networkType.name} IN (${
			networkType.joinToString(
				","
			) { "\"" + it.int + "\"" }
		}) AND ${DownloadTask.Priority.name}<=${priority.value} AND ${DownloadTask.StatusCode}=0 AND ${DownloadTask.Id.name} NOT IN(${
			excluded.joinToString(
				","
			) { "\"" + it + "\"" }
		}) ORDER BY ${DownloadTask.FirstJoinTime.name} DESC LIMIT 1"
		if (excluded.isEmpty()) {
			where = "SELECT ${columns.joinToString(",")} FROM $name WHERE ${DownloadTask.Progress.name} < 100 AND ${DownloadTask.networkType.name} IN (${
				networkType.joinToString(
					","
				) { "\"" + it.int + "\"" }
			}) AND ${DownloadTask.Priority.name}<=${priority.value} AND ${DownloadTask.StatusCode}=0 ORDER BY ${DownloadTask.FirstJoinTime.name} DESC LIMIT 1"
		}
		val cursor = it.UnderlyingDB.rawQuery(where, null)
		if (!cursor.moveToFirst()) {
			cursor.close()
            return@selfDB null
		}

		if (!cursor.moveToFirst()) {
			cursor.close()
            return@selfDB null
		}
		val doc = DownloadTask()
        cursor.ToDownloadTask(doc)
        cursor.close()

        return@selfDB doc
	}

}

suspend fun DownloadTask.Companion.GetProgress(objectId: String):Int {
	if (cache[objectId]!=null) {
		return cache[objectId]!!
	}

	return getUs().selfDB { it ->
        val name = table.TableNameIn(it)
		val where = DownloadTask.Id.eq(objectId)
		val columns = table.AllColumns().map { it.name }.toTypedArray()
		val cursor = it.UnderlyingDB.query(name, columns, where.ArgSQL, where.BindArgs, null, null, null)
		if (!cursor.moveToFirst()) {
			cache[objectId] = 0
            return@selfDB 0
		}


		val doc = DownloadTask()
		cursor.ToDownloadTask(doc)

		cache[objectId] = doc.Progress

		return@selfDB doc.Progress
	}
}


suspend fun DownloadTask.insert(): Error? {
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

suspend fun DownloadTask.Companion.FindById(id: String): DownloadTask? {
	return getUs().selfDB { it ->
        val where = DownloadTask.Id.eq(id)
		val cursor =
			it.UnderlyingDB.query(
				table.TableNameIn(it),
				DownloadTask.AllColumns().map { it.name }.toTypedArray(),
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
		val doc = DownloadTask()
        cursor.ToDownloadTask(doc)
        cursor.close()
        return@selfDB doc
    }

}

suspend fun DownloadTask.SetLastEndTime():Error? {
	return getUs().selfDB {
		val name = table.TableNameIn(it)
		val values = ContentValues()
        values.put(DownloadTask.LastEndTime.name, this.LastEndTime)

		val where = DownloadTask.Id.eq(this.Id)
		val rowId = it.UnderlyingDB.update(name, values, where.ArgSQL, where.BindArgs)
		if (rowId>0) {
			return@selfDB null
		}

		return@selfDB Error("not found")
	}
}

suspend fun DownloadTask.SetLastStartTime():Error? {
	return getUs().selfDB {
		val name = table.TableNameIn(it)
		val values = ContentValues()
        values.put(DownloadTask.LastStartTime.name, this.LastStartTime)

		val where = DownloadTask.Id.eq(this.Id)
		val rowId = it.UnderlyingDB.update(name, values, where.ArgSQL, where.BindArgs)
		if (rowId>0) {
			return@selfDB null
		}

		return@selfDB Error("not found")
	}
}
