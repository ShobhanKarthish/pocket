package com.shobhankarthish.pocket.shelf

import kotlinx.coroutines.Dispatchers
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
                mimeType = "text/plain",
                displayName = "note.txt",
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
    fun removeDeletesLocalCopyAndRow() = runTest {
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
    }
}
