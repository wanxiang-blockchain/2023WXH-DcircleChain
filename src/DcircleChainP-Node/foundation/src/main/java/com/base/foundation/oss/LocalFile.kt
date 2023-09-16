package com.base.foundation.oss

import android.util.Log
import com.base.foundation.getAppContext
import com.base.foundation.utils.Tuple
import com.blankj.utilcode.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.locks.ReentrantLock

interface LocalFile {
    suspend fun write(objectId: String, data: File): Error?
    suspend fun write(objectId: String, input:InputStream):Error?

    fun mv(from: File, to: File): Error?
    fun mv(from: File, objectId: String): Error?

    fun read(objectId: String): Tuple<File, Error?>

    fun has(objectId: String): Boolean

    fun delete(objectId: String): Error?
    fun path(objectId: String): String
}

open class SandboxDBLocalFile : LocalFile {
    private val locks = mutableMapOf<String, ReentrantLock>()

    override fun mv(from: File, to: File): Error? {
        if (!from.exists()) {
            return Error("mv not found(${from})")
        }
        if (to.exists()) {
            to.delete()
        }

        if (from.renameTo(to)) {
            return null
        }

        FileUtils.createOrExistsDir(to.parentFile)
        if (from.renameTo(to)) {
            return null
        }

        return Error("mv failed (form:${from}, to:${to})")
    }

    override fun mv(from: File, objectId: String): Error? {
        return mv(from, File(path(objectId)))
    }

    private fun getLock(objectId: String): ReentrantLock {
        return locks.computeIfAbsent(objectId) { ReentrantLock() }
    }

    override suspend fun write(objectId:String,input: InputStream): Error? = withContext(Dispatchers.IO) {
        if (File(path(objectId)).exists()) {
            return@withContext null
        }

        val lock = getLock(objectId)
        lock.lock()

        val tmp = path(objectId)+".tmp.${this::class.java.name}.write"
        if(!FileUtils.isFileExists(tmp)){
            FileUtils.createOrExistsFile(tmp)
        }

        return@withContext try {
            val output = FileOutputStream(tmp)
            input.copyTo(output)
            output.close()
            mv(File(tmp), File(path(objectId)))
        } catch (e: Exception) {
            Error("Failed to write file: ${e.message}")
        } finally {
        	lock.unlock()
        }
    }

    override suspend fun write(objectId: String, data: File): Error? = withContext(Dispatchers.IO) {
         try {
            return@withContext write(objectId, FileInputStream(data).buffered())
        } catch (e:Exception) {
            return@withContext Error(e.localizedMessage)
        }
    }


    override  fun read(objectId: String): Tuple<File, Error?> {
        val file = File(path(objectId))
        if (!FileUtils.isFileExists(file)){
            return Tuple( file, Error("is not found"))
        }
        return Tuple( file, null)
    }


    override fun has(objectId: String): Boolean {
        val file = File(path(objectId))
        return FileUtils.isFileExists(file)
    }

    override fun delete(objectId: String): Error? {
        Log.w("LocalFile", "delete objectId(${objectId})")
        return try {
            val file = File(path(objectId))
            if (FileUtils.isFileExists(file)) {
                FileUtils.delete(file)
            }
            null
        } catch (e: Exception) {
            Error(e)
        }
    }

    override fun path(objectId: String): String {
        val uid = ""
        val name = "db"
        val absolutePath = getAppContext().applicationContext.getExternalFilesDir(uid)?.absolutePath
        return "$absolutePath${File.separatorChar}${name}${File.separatorChar}${objectId}"
    }

    companion object{
        val default = SandboxDBLocalFile()
    }
}

class SandboxOSSFile : SandboxDBLocalFile() {
    override fun path(objectId: String): String {
        val uid = ""
        val name = "oss"
        val absolutePath = getAppContext().applicationContext.getExternalFilesDir(uid)?.absolutePath
        return "$absolutePath${File.separatorChar}${name}${File.separatorChar}${objectId}"
    }

    companion object{
        val default = SandboxOSSFile()
    }
}


fun GetSandboxDBFile(): LocalFile {
    return SandboxDBLocalFile.default
}

fun GetSandboxOSSFile(): LocalFile {
    return SandboxOSSFile.default
}
