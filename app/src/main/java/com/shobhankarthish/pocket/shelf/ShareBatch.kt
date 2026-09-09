package com.shobhankarthish.pocket.shelf

data class SharePlan(
    val fileItems: List<ShelfItem>,
    val textItems: List<ShelfItem>,
    val skipped: Int,
) {
    val sharedCount: Int get() = fileItems.size + textItems.size
    val isEmpty: Boolean get() = sharedCount == 0
}

sealed interface ShareDecision {
    data object Nothing : ShareDecision

    data class SendFile(val item: ShelfItem) : ShareDecision

    data class SendFiles(
        val items: List<ShelfItem>,
        val mime: String,
        val mixedMimeWarning: Boolean,
    ) : ShareDecision

    data class SendText(
        val items: List<ShelfItem>,
        val body: String,
        val subject: String,
    ) : ShareDecision

    data class Choose(
        val files: ShareDecision,
        val text: SendText,
    ) : ShareDecision

    fun fileCount(): Int = when (this) {
        is SendFile -> 1
        is SendFiles -> items.size
        is Choose -> files.fileCount()
        is SendText, ShareDecision.Nothing -> 0
    }

    fun textCount(): Int = when (this) {
        is SendText -> items.size
        is Choose -> text.items.size
        is SendFile, is SendFiles, ShareDecision.Nothing -> 0
    }
}

data class SharePrep(
    val decision: ShareDecision,
    val skipped: Int,
)

object ShareBatch {
    fun plan(items: List<ShelfItem>, fileReady: (ShelfItem) -> Boolean): SharePlan {
        val skipped = items.count { it.isFileKind && !fileReady(it) }
        val files = items.filter { it.isFileKind && fileReady(it) }
        val texts = items.filter { it.isTextKind }
        return SharePlan(fileItems = files, textItems = texts, skipped = skipped)
    }

    fun decide(
        items: List<ShelfItem>,
        fileReady: (ShelfItem) -> Boolean,
        textBody: (ShelfItem) -> String,
    ): SharePrep {
        val plan = plan(items, fileReady)
        val files = fileDecision(plan.fileItems)
        val text = textDecision(plan.textItems, textBody)
        val decision = when {
            files != null && text != null -> ShareDecision.Choose(files, text)
            files != null -> files
            text != null -> text
            else -> ShareDecision.Nothing
        }
        return SharePrep(decision, plan.skipped)
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

    fun mixedImageAndPdf(files: List<ShelfItem>): Boolean {
        val kinds = files.map { it.kind }.toSet()
        return ItemKind.IMAGE in kinds && ItemKind.PDF in kinds
    }

    private fun fileDecision(files: List<ShelfItem>): ShareDecision? = when {
        files.isEmpty() -> null
        files.size == 1 -> ShareDecision.SendFile(files[0])
        else -> ShareDecision.SendFiles(
            items = files,
            mime = mimeOf(files),
            mixedMimeWarning = mixedImageAndPdf(files),
        )
    }

    private fun textDecision(
        items: List<ShelfItem>,
        textBody: (ShelfItem) -> String,
    ): ShareDecision.SendText? {
        if (items.isEmpty()) return null
        return ShareDecision.SendText(
            items = items,
            body = joinedText(items, textBody),
            subject = items.first().displayName,
        )
    }
}

private val ShelfItem.isFileKind: Boolean
    get() = kind == ItemKind.IMAGE || kind == ItemKind.PDF

private val ShelfItem.isTextKind: Boolean
    get() = kind == ItemKind.TEXT || kind == ItemKind.LINK
