package com.shobhankarthish.pocket.shelf

data class SharePlan(
    val fileItems: List<ShelfItem>,
    val textItems: List<ShelfItem>,
    val skipped: Int,
) {
    val sharedCount: Int get() = fileItems.size + textItems.size
    val isEmpty: Boolean get() = sharedCount == 0
}

object ShareBatch {
    fun plan(items: List<ShelfItem>, fileReady: (ShelfItem) -> Boolean): SharePlan {
        val skipped = items.count { it.isFileKind && !fileReady(it) }
        val files = items.filter { it.isFileKind && fileReady(it) }
        val texts = items.filter { it.isTextKind }
        return SharePlan(fileItems = files, textItems = texts, skipped = skipped)
    }

    fun mimeOf(files: List<ShelfItem>): String {
        if (files.isEmpty()) return TextPayload.TEXT_MIME
        val kinds = files.map { it.kind }.toSet()
        return when {
            kinds == setOf(ItemKind.PDF) -> "application/pdf"
            kinds == setOf(ItemKind.IMAGE) -> {
                val mimes = files.map { it.mimeType }.distinct()
                if (mimes.size == 1) mimes[0] else "image/*"
            }
            else -> "*/*"
        }
    }

    fun joinedText(items: List<ShelfItem>, bodyOf: (ShelfItem) -> String): String =
        items.joinToString("\n\n", transform = bodyOf)
}

private val ShelfItem.isFileKind: Boolean
    get() = kind == ItemKind.IMAGE || kind == ItemKind.PDF

private val ShelfItem.isTextKind: Boolean
    get() = kind == ItemKind.TEXT || kind == ItemKind.LINK
