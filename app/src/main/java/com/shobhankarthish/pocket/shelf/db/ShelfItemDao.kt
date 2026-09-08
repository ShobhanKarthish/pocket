package com.shobhankarthish.pocket.shelf.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShelfItemDao {
    @Query("SELECT * FROM shelf_items ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<ShelfItemEntity>>

    @Query("SELECT * FROM shelf_items ORDER BY createdAtEpochMs DESC")
    suspend fun getAll(): List<ShelfItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShelfItemEntity)

    @Query("DELETE FROM shelf_items WHERE id = :id")
    suspend fun deleteById(id: String)
}
