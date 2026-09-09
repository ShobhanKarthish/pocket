package com.shobhankarthish.pocket.shelf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemovalHoldTest {
    @Test
    fun visibleDropsHeldIdsAndKeepsShelfOrder() {
        val a = item("a")
        val b = item("b")
        val c = item("c")
        val hold = RemovalHold(token = 1, items = listOf(b))
        assertEquals(listOf("a", "c"), hold.visible(listOf(a, b, c)).map { it.id })
        assertTrue(hold.matches(1))
        assertFalse(hold.matches(2))
    }

    @Test
    fun emptyHoldIsIdentity() {
        val a = item("a")
        val hold = RemovalHold(token = 3, items = emptyList())
        assertEquals(listOf("a"), hold.visible(listOf(a)).map { it.id })
    }

    private fun item(id: String) = ShelfItem(
        id = id,
        displayName = id,
        mimeType = "text/plain",
        byteSize = 1,
        relativePath = "$id.txt",
        createdAtEpochMs = 1L,
    )
}
