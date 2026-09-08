package com.shobhankarthish.pocket.shelf.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shobhankarthish.pocket.shelf.ShelfItem

@Entity(tableName = "shelf_items")
data class ShelfItemEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val mimeType: String,
    val byteSize: Long,
    val relativePath: String,
    val createdAtEpochMs: Long,
) {
    fun toDomain(): ShelfItem = ShelfItem(
        id = id,
        displayName = displayName,
        mimeType = mimeType,
        byteSize = byteSize,
        relativePath = relativePath,
        createdAtEpochMs = createdAtEpochMs,
    )

    companion object {
        fun fromDomain(item: ShelfItem): ShelfItemEntity = ShelfItemEntity(
            id = item.id,
            displayName = item.displayName,
            mimeType = item.mimeType,
            byteSize = item.byteSize,
            relativePath = item.relativePath,
            createdAtEpochMs = item.createdAtEpochMs,
        )
    }
}
