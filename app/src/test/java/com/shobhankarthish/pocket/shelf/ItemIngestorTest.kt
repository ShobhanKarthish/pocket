package com.shobhankarthish.pocket.shelf

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream

class ItemIngestorTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private fun harness(): Triple<ItemIngestor, ShelfRepository, ShelfFileStore> {
        val files = ShelfFileStore(tmp.root)
        val repo = ShelfRepository(FakeShelfItemDao(), files)
        val ingestor = ItemIngestor(
            files = files,
            repository = repo,
            io = Dispatchers.Unconfined,
            ids = { "item-1" },
            nowMs = { 1_700_000_000_000L },
        )
        return Triple(ingestor, repo, files)
    }

    @Test
    fun copiesBytesAndPersistsMetadata() = runTest {
        val (ingestor, _, files) = harness()
        val payload = "png-bytes".toByteArray()
        val result = ingestor.ingest(
            InboundFile(
                mimeType = "image/png",
                displayName = "Assignment screenshot.png",
                openStream = { ByteArrayInputStream(payload) },
            ),
        )
        val ok = result as IngestResult.Ok
        assertEquals("Assignment screenshot.png", ok.item.displayName)
        assertEquals("image/png", ok.item.mimeType)
        assertEquals(payload.size.toLong(), ok.item.byteSize)
        assertEquals("item-1.png", ok.item.relativePath)
        assertTrue(files.file("item-1.png").readBytes().contentEquals(payload))
    }

    @Test
    fun rejectsUnsupportedMimeWithoutWriting() = runTest {
        val (ingestor, _, files) = harness()
        val result = ingestor.ingest(
            InboundFile(
                mimeType = "video/mp4",
                displayName = "clip.mp4",
                openStream = { ByteArrayInputStream("hi".toByteArray()) },
            ),
        )
        assertEquals(IngestResult.Unsupported, result)
        assertTrue(files.listNames().isEmpty())
    }

    @Test
    fun failedOpenCleansNothing() = runTest {
        val (ingestor, _, files) = harness()
        val result = ingestor.ingest(
            InboundFile(
                mimeType = "application/pdf",
                displayName = "Assignment.pdf",
                openStream = { null },
            ),
        )
        assertEquals(IngestResult.Failed, result)
        assertTrue(files.listNames().isEmpty())
    }

    @Test
    fun reconcileAfterIngestKeepsFileAndRow() = runTest {
        val (ingestor, repo, files) = harness()
        val ok = ingestor.ingest(
            InboundFile(
                mimeType = "image/png",
                displayName = "pocket-demo.png",
                openStream = { ByteArrayInputStream("png".toByteArray()) },
            ),
        ) as IngestResult.Ok
        repo.reconcile()
        assertTrue(files.file(ok.item.relativePath).exists())
        assertEquals(1, repo.observeItems().first().size)
    }

    @Test
    fun removeLastItemLeavesEmptyObservation() = runTest {
        val (ingestor, repo, files) = harness()
        val ok = ingestor.ingest(
            InboundFile(
                mimeType = "application/pdf",
                displayName = "Assignment.pdf",
                openStream = { ByteArrayInputStream("pdf".toByteArray()) },
            ),
        ) as IngestResult.Ok
        assertTrue(files.file(ok.item.relativePath).exists())
        repo.remove(ok.item)
        assertFalse(files.file(ok.item.relativePath).exists())
        repo.reconcile()
        assertTrue(files.listNames().isEmpty())
        assertTrue(repo.observeItems().first().isEmpty())
    }

    @Test
    fun ingestTextPersistsProse() = runTest {
        val (ingestor, repo, files) = harness()
        val ok = ingestor.ingestText("Pack the bag\nDon't forget socks") as IngestResult.Ok
        assertEquals(ItemKind.TEXT, ok.item.kind)
        assertEquals("Pack the bag", ok.item.displayName)
        assertEquals(TextPayload.TEXT_MIME, ok.item.mimeType)
        assertEquals("item-1.txt", ok.item.relativePath)
        assertEquals("Pack the bag\nDon't forget socks", files.file("item-1.txt").readText())
        assertEquals("TEXT · 31 B", ok.item.metaLine)
        repo.reconcile()
        assertEquals(1, repo.observeItems().first().size)
    }

    @Test
    fun ingestTextPersistsLink() = runTest {
        val (ingestor, repo, files) = harness()
        val ok = ingestor.ingestText("https://example.com/notes") as IngestResult.Ok
        assertEquals(ItemKind.LINK, ok.item.kind)
        assertEquals("https://example.com/notes", ok.item.displayName)
        assertEquals("example.com", ok.item.titleLine)
        assertEquals(TextPayload.LINK_MIME, ok.item.mimeType)
        assertEquals("item-1.url", ok.item.relativePath)
        assertEquals("https://example.com/notes", files.file("item-1.url").readText())
        assertEquals("https://example.com/notes", ok.item.metaLine)
        repo.remove(ok.item)
        assertTrue(files.listNames().isEmpty())
        assertTrue(repo.observeItems().first().isEmpty())
    }

    @Test
    fun ingestBlankTextFails() = runTest {
        val (ingestor, _, files) = harness()
        assertEquals(IngestResult.Failed, ingestor.ingestText("   "))
        assertTrue(files.listNames().isEmpty())
    }
}
