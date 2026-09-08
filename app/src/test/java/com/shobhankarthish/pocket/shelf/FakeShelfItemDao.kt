package com.shobhankarthish.pocket.shelf

import com.shobhankarthish.pocket.shelf.db.ShelfItemDao
import com.shobhankarthish.pocket.shelf.db.ShelfItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeShelfItemDao : ShelfItemDao {
    private val items = MutableStateFlow<List<ShelfItemEntity>>(emptyList())

    override fun observeAll(): Flow<List<ShelfItemEntity>> =
        items.map { rows -> rows.sortedByDescending { it.createdAtEpochMs } }

    override suspend fun getAll(): List<ShelfItemEntity> =
        items.value.sortedByDescending { it.createdAtEpochMs }

    override suspend fun insert(item: ShelfItemEntity) {
        items.update { current -> current.filterNot { it.id == item.id } + item }
    }

    override suspend fun deleteById(id: String) {
        items.update { current -> current.filterNot { it.id == id } }
    }
}
