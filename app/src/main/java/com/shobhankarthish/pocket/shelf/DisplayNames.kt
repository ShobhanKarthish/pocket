package com.shobhankarthish.pocket.shelf

object DisplayNames {
    fun sanitize(raw: String?, kind: ItemKind): String {
        val trimmed = raw?.substringAfterLast('/')?.trim().orEmpty()
        val cleaned = trimmed.replace('\\', '_').replace('/', '_')
        return cleaned.ifBlank { fallback(kind) }
    }

    fun fallback(kind: ItemKind): String = when (kind) {
        ItemKind.IMAGE -> "image.jpg"
        ItemKind.PDF -> "document.pdf"
        ItemKind.TEXT -> "note.txt"
        ItemKind.LINK -> "link.url"
    }

    fun extensionOf(name: String, kind: ItemKind): String {
        val ext = name.substringAfterLast('.', missingDelimiterValue = "")
            .lowercase()
            .filter { it.isLetterOrDigit() }
        if (ext.isNotEmpty() && ext.length in 1..8) return ext
        return when (kind) {
            ItemKind.IMAGE -> "jpg"
            ItemKind.PDF -> "pdf"
            ItemKind.TEXT -> "txt"
            ItemKind.LINK -> "url"
        }
    }
}
