package com.shobhankarthish.pocket.shelf

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream

class ShelfRepositoryOrderTest {
    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun newestIngestSitsOnTopAndReorderPersists() = runTest {
        var n = 0
        val files = ShelfFileStore(tmp.root)
        val repo = ShelfRepository(FakeShelfItemDao(), files)
        val ingestor = ItemIngestor(
            files = files,
            repository = repo,
            io = Dispatchers.Unconfined,
            ids = { "item-${++n}" },
            nowMs = { n * 1_000L },
        )
        ingestPng(ingestor, "first.png")
        ingestPng(ingestor, "second.png")
        ingestPng(ingestor, "third.png")

        val newestFirst = repo.observeItems().first().map { it.displayName }
        assertEquals(listOf("third.png", "second.png", "first.png"), newestFirst)

        val ids = repo.observeItems().first().map { it.id }
        repo.reorder(listOf(ids[2], ids[0], ids[1]))
        assertEquals(
            listOf("first.png", "third.png", "second.png"),
            repo.observeItems().first().map { it.displayName },
        )
    }

    private suspend fun ingestPng(ingestor: ItemIngestor, name: String) {
        ingestor.ingest(
            InboundFile(
                mimeType = "image/png",
                displayName = name,
                openStream = { ByteArrayInputStream(name.toByteArray()) },
            ),
        )
    }
}
