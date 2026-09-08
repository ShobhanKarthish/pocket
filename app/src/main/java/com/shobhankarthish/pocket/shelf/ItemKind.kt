package com.shobhankarthish.pocket.shelf

enum class ItemKind {
    IMAGE,
    PDF,
    TEXT,
    LINK,
    ;

    companion object {
        fun fromMime(mime: String?): ItemKind? {
            val normalized = mime?.substringBefore(';')?.trim()?.lowercase().orEmpty()
            return when {
                normalized == "application/pdf" -> PDF
                normalized == TextPayload.TEXT_MIME -> TEXT
                normalized == TextPayload.LINK_MIME || normalized == "text/uri-list" -> LINK
                normalized.startsWith("image/") && normalized.length > "image/".length -> IMAGE
                else -> null
            }
        }
    }
}
