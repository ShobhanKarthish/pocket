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
        items.map { rows -> rows.sortedWith(ORDER) }

    override suspend fun getAll(): List<ShelfItemEntity> =
        items.value.sortedWith(ORDER)

    override suspend fun maxSortIndex(): Long =
        items.value.maxOfOrNull { it.sortIndex } ?: 0L

    override suspend fun insert(item: ShelfItemEntity) {
        items.update { current -> current.filterNot { it.id == item.id } + item }
    }

    override suspend fun updateSortIndex(id: String, sortIndex: Long) {
        items.update { current ->
            current.map { if (it.id == id) it.copy(sortIndex = sortIndex) else it }
        }
    }

    override suspend fun deleteById(id: String) {
        items.update { current -> current.filterNot { it.id == id } }
    }

    private companion object {
        val ORDER = compareByDescending<ShelfItemEntity> { it.sortIndex }
            .thenByDescending { it.createdAtEpochMs }
    }
}
