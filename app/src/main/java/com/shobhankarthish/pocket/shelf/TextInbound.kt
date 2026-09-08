package com.shobhankarthish.pocket.shelf

import java.net.URI

sealed class TextPayload {
    abstract val body: String
    abstract val displayName: String
    abstract val mimeType: String
    abstract val extension: String
    abstract val kind: ItemKind

    data class Prose(
        override val body: String,
        override val displayName: String,
    ) : TextPayload() {
        override val mimeType: String = TEXT_MIME
        override val extension: String = "txt"
        override val kind: ItemKind = ItemKind.TEXT
    }

    data class Link(
        val url: String,
        val host: String,
    ) : TextPayload() {
        override val body: String = url
        override val displayName: String = url
        override val mimeType: String = LINK_MIME
        override val extension: String = "url"
        override val kind: ItemKind = ItemKind.LINK
    }

    companion object {
        const val TEXT_MIME = "text/plain"
        const val LINK_MIME = "text/x-uri"
    }
}

object TextInbound {
    fun parse(raw: String): TextPayload? {
        val body = raw.trim()
        if (body.isEmpty()) return null
        val url = singleHttpUrl(body)
        return if (url != null) {
            TextPayload.Link(url = url, host = hostOf(url) ?: url)
        } else {
            TextPayload.Prose(body = body, displayName = titleOf(body))
        }
    }

    fun hostOf(url: String): String? = singleHttpUrl(url)?.let { parsed ->
        runCatching { URI(parsed).host }.getOrNull()?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun singleHttpUrl(raw: String): String? {
        if (raw.any { it.isWhitespace() }) return null
        val uri = runCatching { URI(raw) }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase() ?: return null
        if (scheme != "http" && scheme != "https") return null
        val host = uri.host?.trim().orEmpty()
        if (host.isEmpty()) return null
        return raw
    }

    fun titleOf(body: String): String {
        val line = body.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
        val clipped = line.ifBlank { "Note" }
        return if (clipped.length <= 80) clipped else clipped.take(80)
    }
}
