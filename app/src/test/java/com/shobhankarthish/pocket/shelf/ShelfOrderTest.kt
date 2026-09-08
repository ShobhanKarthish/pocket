package com.shobhankarthish.pocket.shelf

import org.junit.Assert.assertEquals
import org.junit.Test

class ShelfOrderTest {
    @Test
    fun moveUpSwapsWithPreviousInShelfOrder() {
        assertEquals(
            listOf("b", "a", "c"),
            ShelfOrder.move(listOf("a", "b", "c"), "b", -1),
        )
    }

    @Test
    fun moveDownSwapsWithNextInShelfOrder() {
        assertEquals(
            listOf("a", "c", "b"),
            ShelfOrder.move(listOf("a", "b", "c"), "b", 1),
        )
    }

    @Test
    fun movePastEndsIsNoOp() {
        val ids = listOf("a", "b")
        assertEquals(ids, ShelfOrder.move(ids, "a", -1))
        assertEquals(ids, ShelfOrder.move(ids, "b", 1))
        assertEquals(ids, ShelfOrder.move(ids, "missing", 1))
    }

    @Test
    fun moveToReinsertsAtTargetIndex() {
        assertEquals(
            listOf("c", "a", "b"),
            ShelfOrder.moveTo(listOf("a", "b", "c"), from = 2, to = 0),
        )
    }

    @Test
    fun sortIndexesPutFirstItemHighest() {
        assertEquals(
            mapOf("a" to 3L, "b" to 2L, "c" to 1L),
            ShelfOrder.sortIndexes(listOf("a", "b", "c")),
        )
    }
}
