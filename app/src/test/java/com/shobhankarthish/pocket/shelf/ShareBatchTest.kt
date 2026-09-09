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

    @Test
    fun oneFileDecidesSend() {
        val png = fileItem("a", "image/png")
        val prep = ShareBatch.decide(listOf(png), { true }, { it.displayName })
        val send = prep.decision as ShareDecision.SendFile
        assertEquals("a", send.item.id)
        assertEquals(0, prep.skipped)
    }

    @Test
    fun severalImagesDecideSendMultiple() {
        val png = fileItem("a", "image/png")
        val jpg = fileItem("b", "image/jpeg")
        val prep = ShareBatch.decide(listOf(png, jpg), { true }, { it.displayName })
        val send = prep.decision as ShareDecision.SendFiles
        assertEquals(listOf("a", "b"), send.items.map { it.id })
        assertEquals("image/*", send.mime)
        assertTrue(!send.mixedMimeWarning)
    }

    @Test
    fun severalPdfsDecideSendMultiple() {
        val a = fileItem("a", "application/pdf")
        val b = fileItem("b", "application/pdf")
        val prep = ShareBatch.decide(listOf(a, b), { true }, { it.displayName })
        val send = prep.decision as ShareDecision.SendFiles
        assertEquals("application/pdf", send.mime)
        assertTrue(!send.mixedMimeWarning)
    }

    @Test
    fun imagesAndPdfsWarnOnWildcardMime() {
        val png = fileItem("a", "image/png")
        val pdf = fileItem("b", "application/pdf")
        val prep = ShareBatch.decide(listOf(png, pdf), { true }, { it.displayName })
        val send = prep.decision as ShareDecision.SendFiles
        assertEquals("*/*", send.mime)
        assertTrue(send.mixedMimeWarning)
        assertTrue(ShareBatch.mixedImageAndPdf(send.items))
    }

    @Test
    fun textAndLinksCombineInShelfOrder() {
        val note = textItem("n", "Pack the bag")
        val link = linkItem("l", "https://example.com/notes")
        val prep = ShareBatch.decide(listOf(note, link), { false }, { it.displayName })
        val send = prep.decision as ShareDecision.SendText
        assertEquals(listOf("n", "l"), send.items.map { it.id })
        assertEquals("Pack the bag\n\nhttps://example.com/notes", send.body)
        assertEquals("Pack the bag", send.subject)
    }

    @Test
    fun filesPlusTextDecideChooseAndKeepText() {
        val png = fileItem("a", "image/png")
        val note = textItem("n", "hello")
        val link = linkItem("l", "https://example.com/notes")
        val prep = ShareBatch.decide(listOf(png, note, link), { true }, { it.displayName })
        val choose = prep.decision as ShareDecision.Choose
        val files = choose.files as ShareDecision.SendFile
        assertEquals("a", files.item.id)
        assertEquals(listOf("n", "l"), choose.text.items.map { it.id })
        assertEquals("hello\n\nhttps://example.com/notes", choose.text.body)
        assertEquals(0, prep.skipped)
    }

    @Test
    fun missingFilePlusTextStillOffersText() {
        val gone = fileItem("f", "image/png")
        val note = textItem("n", "keep me")
        val prep = ShareBatch.decide(listOf(gone, note), { false }, { it.displayName })
        val send = prep.decision as ShareDecision.SendText
        assertEquals(listOf("n"), send.items.map { it.id })
        assertEquals("keep me", send.body)
        assertEquals(1, prep.skipped)
    }

    @Test
    fun emptySelectionIsNothing() {
        val prep = ShareBatch.decide(emptyList(), { true }, { it.displayName })
        assertEquals(ShareDecision.Nothing, prep.decision)
        assertEquals(0, prep.skipped)
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
