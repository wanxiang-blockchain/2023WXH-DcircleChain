package com.base.foundation.db

import android.content.ContentValues
import android.util.Log
import com.base.foundation.getUs
import com.github.xpwu.ktdbtable.invoke
import com.github.xpwu.ktdbtble.annotation.Column
import com.github.xpwu.ktdbtble.annotation.Index
import com.github.xpwu.ktdbtble.annotation.PrimaryKey
import com.github.xpwu.ktdbtble.annotation.Table
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

private val table = DBLog.Companion
@Table("DBLog")
class DBLog {
	@Column("No", primaryKey = PrimaryKey.ONLY_ONE_AUTO_INC)
	var No: Long = 0

	@Index
    @Column("Tag")
	var Tag:String = ""

	@Column("Text")
	var Text: String = ""

	@Column("Time")
	var Time:Long = 0

	companion object
}

fun DBLog.Companion.Insert(tag:String, text:String) {
	Log.d(tag, text)
	CoroutineScope(Dispatchers.IO).launch {
		Log.d(tag, text)
		val values = ContentValues()
		values.put(DBLog.Tag.name, tag)
		values.put(DBLog.Time.name, Date().time)
		values.put(DBLog.Text.name, text)

		getUs().shareDB.invoke {
			it.UnderlyingDB.insert(table.TableNameIn(it), null, values)
		}
	}
}
