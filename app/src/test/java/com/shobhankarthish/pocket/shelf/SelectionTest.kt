package com.shobhankarthish.pocket.shelf

import org.junit.Assert.assertEquals
import org.junit.Test

class SelectionTest {
    @Test
    fun toggleAddsThenRemoves() {
        val once = Selection.toggle(emptySet(), "a")
        assertEquals(setOf("a"), once)
        assertEquals(emptySet<String>(), Selection.toggle(once, "a"))
    }

    @Test
    fun inShelfOrderKeepsListOrderNotTapOrder() {
        val items = listOf(
            shelfItem("a"),
            shelfItem("b"),
            shelfItem("c"),
        )
        assertEquals(
            listOf("a", "c"),
            Selection.inShelfOrder(items, setOf("c", "a")).map { it.id },
        )
    }

    @Test
    fun allAndNone() {
        assertEquals(setOf("a", "b"), Selection.all(listOf("a", "b")))
        assertEquals(emptySet<String>(), Selection.none())
    }

    private fun shelfItem(id: String) = ShelfItem(
        id = id,
        displayName = id,
        mimeType = "image/png",
        byteSize = 1,
        relativePath = "$id.png",
        createdAtEpochMs = 1L,
    )
}
