package com.shobhankarthish.pocket.shelf

data class ShelfItem(
    val id: String,
    val displayName: String,
    val mimeType: String,
    val byteSize: Long,
    val relativePath: String,
    val createdAtEpochMs: Long,
) {
    val kind: ItemKind
        get() = ItemKind.fromMime(mimeType) ?: ItemKind.PDF

    val typeLabel: String
        get() = when (kind) {
            ItemKind.PDF -> "PDF"
            ItemKind.TEXT -> "TEXT"
            ItemKind.LINK -> "LINK"
            ItemKind.IMAGE -> mimeType.substringAfter('/', "IMG")
                .substringBefore('+')
                .substringBefore(';')
                .uppercase()
        }

    val titleLine: String
        get() = when (kind) {
            ItemKind.LINK -> TextInbound.hostOf(displayName) ?: displayName
            else -> displayName
        }

    val metaLine: String
        get() = when (kind) {
            ItemKind.LINK -> displayName
            else -> "$typeLabel \u00B7 ${ByteSizeFormatter.format(byteSize)}"
        }
}
