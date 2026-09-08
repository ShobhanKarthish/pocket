package com.shobhankarthish.pocket.shelf

import com.shobhankarthish.pocket.shelf.db.ShelfItemDao
import com.shobhankarthish.pocket.shelf.db.ShelfItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.InputStream

class ShelfRepository(
    private val dao: ShelfItemDao,
    private val files: ShelfFileStore,
) {
    private val mutex = Mutex()

    fun observeItems(): Flow<List<ShelfItem>> =
        dao.observeAll().map { rows -> rows.map(ShelfItemEntity::toDomain) }

    suspend fun add(relativePath: String, input: InputStream, item: ShelfItem): ShelfItem =
        mutex.withLock {
            try {
                val size = files.write(relativePath, input)
                val stored = item.copy(byteSize = size)
                dao.insert(ShelfItemEntity.fromDomain(stored))
                stored
            } catch (t: Throwable) {
                files.delete(relativePath)
                throw t
            }
        }

    suspend fun remove(item: ShelfItem) {
        mutex.withLock {
            files.delete(item.relativePath)
            dao.deleteById(item.id)
        }
    }

    fun fileFor(item: ShelfItem): File = files.file(item.relativePath)

    suspend fun reconcile() {
        mutex.withLock {
            val rows = dao.getAll()
            val onDisk = files.listNames()
            rows.filter { it.relativePath !in onDisk }.forEach { dao.deleteById(it.id) }
            onDisk.filter { name -> rows.none { it.relativePath == name } }
                .forEach { files.delete(it) }
        }
    }
}
