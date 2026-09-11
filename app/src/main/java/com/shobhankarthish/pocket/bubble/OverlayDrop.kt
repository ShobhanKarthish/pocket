package com.shobhankarthish.pocket.bubble

import android.content.ClipDescription
import android.net.Uri
import android.view.DragEvent
import com.shobhankarthish.pocket.shelf.ItemKind

data class OverlayDropPayload(
    val uris: List<Uri>,
    val texts: List<String>,
)

sealed interface OverlayDropPlan {
    data class Streams(val uris: List<Uri>) : OverlayDropPlan
    data class Text(val text: String) : OverlayDropPlan
    data object Empty : OverlayDropPlan
}

object OverlayDrop {
    fun acceptsMime(mime: String?): Boolean {
        val normalized = mime?.substringBefore(';')?.trim()?.lowercase().orEmpty()
        if (normalized.isEmpty()) return false
        if (normalized == "*/*") return true
        if (normalized == ClipDescription.MIMETYPE_TEXT_PLAIN) return true
        if (normalized == ClipDescription.MIMETYPE_TEXT_HTML) return true
        if (normalized == ClipDescription.MIMETYPE_TEXT_URILIST) return true
        if (normalized.startsWith("image/")) return true
        return ItemKind.fromMime(normalized) != null
    }

    fun accepts(description: ClipDescription?): Boolean {
        if (description == null) return true
        if (description.mimeTypeCount == 0) return true
        for (i in 0 until description.mimeTypeCount) {
            if (acceptsMime(description.getMimeType(i))) return true
        }
        return false
    }

    fun plan(uris: List<Uri>, texts: List<String>): OverlayDropPlan {
        if (uris.isNotEmpty()) return OverlayDropPlan.Streams(uris)
        val body = texts.map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n\n")
        if (body.isNotEmpty()) return OverlayDropPlan.Text(body)
        return OverlayDropPlan.Empty
    }

    fun snapshot(event: DragEvent): OverlayDropPayload {
        val clip = event.clipData ?: return OverlayDropPayload(emptyList(), emptyList())
        val uris = ArrayList<Uri>(clip.itemCount)
        val texts = ArrayList<String>(clip.itemCount)
        for (i in 0 until clip.itemCount) {
            val item = clip.getItemAt(i)
            item.uri?.let(uris::add)
            item.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let(texts::add)
        }
        return OverlayDropPayload(uris, texts)
    }
}
