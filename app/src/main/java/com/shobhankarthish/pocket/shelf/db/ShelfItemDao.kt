package com.shobhankarthish.pocket.shelf.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShelfItemDao {
    @Query("SELECT * FROM shelf_items ORDER BY sortIndex DESC, createdAtEpochMs DESC")
    fun observeAll(): Flow<List<ShelfItemEntity>>

    @Query("SELECT * FROM shelf_items ORDER BY sortIndex DESC, createdAtEpochMs DESC")
    suspend fun getAll(): List<ShelfItemEntity>

    @Query("SELECT COALESCE(MAX(sortIndex), 0) FROM shelf_items")
    suspend fun maxSortIndex(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShelfItemEntity)

    @Query("UPDATE shelf_items SET sortIndex = :sortIndex WHERE id = :id")
    suspend fun updateSortIndex(id: String, sortIndex: Long)

    @Query("DELETE FROM shelf_items WHERE id = :id")
    suspend fun deleteById(id: String)
}
