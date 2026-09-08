package com.shobhankarthish.pocket.shelf

import com.shobhankarthish.pocket.shelf.db.ShelfItemDao
import com.shobhankarthish.pocket.shelf.db.ShelfItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class ShelfRepository(
    private val dao: ShelfItemDao,
    private val files: ShelfFileStore,
) {
    fun observeItems(): Flow<List<ShelfItem>> =
        dao.observeAll().map { rows -> rows.map(ShelfItemEntity::toDomain) }

    suspend fun insert(item: ShelfItem) {
        dao.insert(ShelfItemEntity.fromDomain(item))
    }

    suspend fun remove(item: ShelfItem) {
        files.delete(item.relativePath)
        dao.deleteById(item.id)
    }

    fun fileFor(item: ShelfItem): File = files.file(item.relativePath)

    suspend fun reconcile() {
        val rows = dao.getAll()
        val onDisk = files.listNames()
        rows.filter { it.relativePath !in onDisk }.forEach { dao.deleteById(it.id) }
        onDisk.filter { name -> rows.none { it.relativePath == name } }
            .forEach { files.delete(it) }
    }
}
