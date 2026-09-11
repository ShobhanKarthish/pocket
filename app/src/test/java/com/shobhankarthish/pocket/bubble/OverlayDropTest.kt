package com.shobhankarthish.pocket.bubble

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayDropTest {
    @Test
    fun acceptsImagesPdfAndText() {
        assertTrue(OverlayDrop.acceptsMime("image/png"))
        assertTrue(OverlayDrop.acceptsMime("image/jpeg; charset=binary"))
        assertTrue(OverlayDrop.acceptsMime("application/pdf"))
        assertTrue(OverlayDrop.acceptsMime("text/plain"))
        assertTrue(OverlayDrop.acceptsMime("text/x-uri"))
        assertTrue(OverlayDrop.acceptsMime("text/uri-list"))
        assertTrue(OverlayDrop.acceptsMime("*/*"))
        assertFalse(OverlayDrop.acceptsMime("video/mp4"))
        assertFalse(OverlayDrop.acceptsMime("audio/mpeg"))
        assertFalse(OverlayDrop.acceptsMime(""))
        assertFalse(OverlayDrop.acceptsMime(null))
    }

    @Test
    fun textJoinsInOrderWhenNoUris() {
        val plan = OverlayDrop.plan(emptyList(), listOf("  one  ", "", "two"))
        assertEquals(OverlayDropPlan.Text("one\n\ntwo"), plan)
    }

    @Test
    fun emptyWhenNothingUsable() {
        assertEquals(OverlayDropPlan.Empty, OverlayDrop.plan(emptyList(), listOf("  ", "")))
        assertEquals(OverlayDropPlan.Empty, OverlayDrop.plan(emptyList(), emptyList()))
    }
}
