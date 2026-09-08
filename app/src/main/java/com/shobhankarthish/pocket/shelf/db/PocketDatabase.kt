package com.shobhankarthish.pocket.shelf.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ShelfItemEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class PocketDatabase : RoomDatabase() {
    abstract fun shelfItemDao(): ShelfItemDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE shelf_items ADD COLUMN sortIndex INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE shelf_items SET sortIndex = createdAtEpochMs")
            }
        }
    }
}
