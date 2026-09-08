package com.shobhankarthish.pocket.shelf

import java.io.File
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
        dest.outputStream().use { output ->
            return input.copyTo(output)
        }
    }

    fun delete(relativePath: String) {
        if (!isSafeRelativePath(relativePath)) return
        file(relativePath).delete()
    }

    fun listNames(): Set<String> = shelfDir.list()?.toSet() ?: emptySet()

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
