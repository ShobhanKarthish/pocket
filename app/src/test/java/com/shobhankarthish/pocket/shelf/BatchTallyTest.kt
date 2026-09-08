package com.shobhankarthish.pocket.shelf

import org.junit.Assert.assertEquals
import org.junit.Test

class BatchTallyTest {
    @Test
    fun plusCountsEachIngestResult() {
        val tally = BatchTally()
            .plus(IngestResult.Ok(item()))
            .plus(IngestResult.Ok(item()))
            .plus(IngestResult.Unsupported)
            .plus(IngestResult.Failed)
        assertEquals(2, tally.added)
        assertEquals(1, tally.unsupported)
        assertEquals(1, tally.failed)
        assertEquals(4, tally.attempted)
    }

    private fun item() = ShelfItem(
        id = "x",
        displayName = "x.png",
        mimeType = "image/png",
        byteSize = 1,
        relativePath = "x.png",
        createdAtEpochMs = 1L,
    )
}
