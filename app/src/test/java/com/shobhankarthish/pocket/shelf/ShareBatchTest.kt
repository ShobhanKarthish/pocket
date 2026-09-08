package com.shobhankarthish.pocket.shelf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareBatchTest {
    @Test
    fun filesUseSendMultipleMimeAndSkipMissing() {
        val png = fileItem("a", "image/png")
        val jpg = fileItem("b", "image/jpeg")
        val missing = fileItem("c", "application/pdf")
        val plan = ShareBatch.plan(listOf(png, jpg, missing)) { it.id != "c" }
        assertEquals(listOf("a", "b"), plan.fileItems.map { it.id })
        assertTrue(plan.textItems.isEmpty())
        assertEquals(1, plan.skipped)
        assertEquals("image/*", ShareBatch.mimeOf(plan.fileItems))
    }

    @Test
    fun mixedImageAndPdfUsesWildcardMime() {
        val plan = ShareBatch.plan(
            listOf(fileItem("a", "image/png"), fileItem("b", "application/pdf")),
        ) { true }
        assertEquals("*/*", ShareBatch.mimeOf(plan.fileItems))
    }

    @Test
    fun samePdfMimeStaysPdf() {
        val plan = ShareBatch.plan(
            listOf(fileItem("a", "application/pdf"), fileItem("b", "application/pdf")),
        ) { true }
        assertEquals("application/pdf", ShareBatch.mimeOf(plan.fileItems))
    }

    @Test
    fun textAndLinkStayOnTextPathAndKeepShelfOrder() {
        val note = textItem("n", "Pack the bag")
        val link = linkItem("l", "https://example.com/notes")
        val plan = ShareBatch.plan(listOf(note, link)) { false }
        assertTrue(plan.fileItems.isEmpty())
        assertEquals(listOf("n", "l"), plan.textItems.map { it.id })
        assertEquals(0, plan.skipped)
        assertEquals(
            "Pack the bag\n\nhttps://example.com/notes",
            ShareBatch.joinedText(plan.textItems) { it.displayName },
        )
    }

    @Test
    fun missingFilesDoNotDropTextItems() {
        val note = textItem("n", "hello")
        val gone = fileItem("f", "image/png")
        val plan = ShareBatch.plan(listOf(note, gone)) { false }
        assertEquals(listOf("n"), plan.textItems.map { it.id })
        assertEquals(1, plan.skipped)
        assertEquals(1, plan.sharedCount)
    }

    private fun fileItem(id: String, mime: String) = ShelfItem(
        id = id,
        displayName = "$id.bin",
        mimeType = mime,
        byteSize = 4,
        relativePath = "$id.bin",
        createdAtEpochMs = 1L,
    )

    private fun textItem(id: String, body: String) = ShelfItem(
        id = id,
        displayName = body,
        mimeType = TextPayload.TEXT_MIME,
        byteSize = body.length.toLong(),
        relativePath = "$id.txt",
        createdAtEpochMs = 1L,
    )

    private fun linkItem(id: String, url: String) = ShelfItem(
        id = id,
        displayName = url,
        mimeType = TextPayload.LINK_MIME,
        byteSize = url.length.toLong(),
        relativePath = "$id.url",
        createdAtEpochMs = 1L,
    )
}
