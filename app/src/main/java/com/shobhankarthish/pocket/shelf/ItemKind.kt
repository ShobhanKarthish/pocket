package com.shobhankarthish.pocket.shelf

enum class ItemKind {
    IMAGE,
    PDF,
    ;

    companion object {
        fun fromMime(mime: String?): ItemKind? {
            val normalized = mime?.substringBefore(';')?.trim()?.lowercase().orEmpty()
            return when {
                normalized == "application/pdf" -> PDF
                normalized.startsWith("image/") && normalized.length > "image/".length -> IMAGE
                else -> null
            }
        }
    }
}
