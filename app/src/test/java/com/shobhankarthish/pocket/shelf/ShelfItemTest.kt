package com.shobhankarthish.pocket.shelf

import org.junit.Assert.assertEquals
import org.junit.Test

class ShelfItemTest {
    @Test
    fun linkRowUsesHostnameTitleAndUrlSubtitle() {
        val item = shelfItem(
            displayName = "https://example.com/notes",
            mimeType = TextPayload.LINK_MIME,
            byteSize = 26,
        )
        assertEquals("example.com", item.titleLine)
        assertEquals("https://example.com/notes", item.metaLine)
        assertEquals("LINK", item.typeLabel)
    }

    @Test
    fun textRowKeepsFirstLineAndSizeMeta() {
        val item = shelfItem(
            displayName = "Pack the bag. Do not forget socks.",
            mimeType = TextPayload.TEXT_MIME,
            byteSize = 34,
        )
        assertEquals("Pack the bag. Do not forget socks.", item.titleLine)
        assertEquals("TEXT · 34 B", item.metaLine)
    }

    @Test
    fun fileRowsKeepNameAndTypeSizeMeta() {
        val pdf = shelfItem(
            displayName = "pocket-demo.pdf",
            mimeType = "application/pdf",
            byteSize = 539,
        )
        val png = shelfItem(
            displayName = "pocket-demo.png",
            mimeType = "image/png",
            byteSize = 226,
        )
        assertEquals("pocket-demo.pdf", pdf.titleLine)
        assertEquals("PDF · 539 B", pdf.metaLine)
        assertEquals("pocket-demo.png", png.titleLine)
        assertEquals("PNG · 226 B", png.metaLine)
    }

    private fun shelfItem(
        displayName: String,
        mimeType: String,
        byteSize: Long,
    ) = ShelfItem(
        id = "item-1",
        displayName = displayName,
        mimeType = mimeType,
        byteSize = byteSize,
        relativePath = "item-1.bin",
        createdAtEpochMs = 1_700_000_000_000L,
    )
}
