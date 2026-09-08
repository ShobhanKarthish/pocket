package com.shobhankarthish.pocket.shelf.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ShelfItemEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class PocketDatabase : RoomDatabase() {
    abstract fun shelfItemDao(): ShelfItemDao
}
