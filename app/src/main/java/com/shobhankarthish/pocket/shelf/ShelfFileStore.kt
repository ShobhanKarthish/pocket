package com.shobhankarthish.pocket.shelf

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ShelfFileStore(private val filesDir: File) {
    val shelfDir: File
        get() = File(filesDir, DIR).also { it.mkdirs() }

    fun file(relativePath: String): File {
        require(isSafeRelativePath(relativePath)) { "unsafe path" }
        return File(shelfDir, relativePath)
    }

    fun write(relativePath: String, input: InputStream): Long {
        val dest = file(relativePath)
        dest.parentFile?.mkdirs()
        val tmp = File(dest.parentFile, "${dest.name}.part")
        try {
            val size = FileOutputStream(tmp).use { output ->
                val copied = input.copyTo(output)
                output.flush()
                output.fd.sync()
                copied
            }
            if (!tmp.renameTo(dest)) {
                tmp.copyTo(dest, overwrite = true)
                tmp.delete()
            }
            return size
        } catch (t: Throwable) {
            tmp.delete()
            dest.delete()
            throw t
        }
    }

    fun delete(relativePath: String) {
        if (!isSafeRelativePath(relativePath)) return
        file(relativePath).delete()
    }

    fun listNames(): Set<String> =
        shelfDir.list()?.filterNot { it.endsWith(".part") }?.toSet() ?: emptySet()

    companion object {
        const val DIR = "shelf"

        fun isSafeRelativePath(relativePath: String): Boolean {
            if (relativePath.isBlank()) return false
            if (relativePath.contains('/') || relativePath.contains('\\')) return false
            if (relativePath.contains("..")) return false
            return true
        }
    }
}
