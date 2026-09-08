package com.shobhankarthish.pocket.shelf

object InboundMimes {
    fun resolve(resolverType: String?, displayName: String?): String? {
        val fromResolver = resolverType?.substringBefore(';')?.trim()?.lowercase().orEmpty()
        if (fromResolver.isNotEmpty() && fromResolver != "application/octet-stream") {
            return fromResolver
        }
        val ext = displayName
            ?.substringAfterLast('/')
            ?.substringAfterLast('.', "")
            ?.lowercase()
            .orEmpty()
        return when (ext) {
            "pdf" -> "application/pdf"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "heic", "heif" -> "image/heic"
            "bmp" -> "image/bmp"
            else -> fromResolver.ifEmpty { null }
        }
    }
}
