package com.shobhankarthish.pocket.shelf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TextInboundTest {
    @Test
    fun blankIsRejected() {
        assertNull(TextInbound.parse("  \n"))
    }

    @Test
    fun httpsUrlIsLink() {
        val parsed = TextInbound.parse("https://example.com/path?q=1") as TextPayload.Link
        assertEquals("https://example.com/path?q=1", parsed.url)
        assertEquals("example.com", parsed.host)
        assertEquals(ItemKind.LINK, parsed.kind)
        assertEquals(TextPayload.LINK_MIME, parsed.mimeType)
    }

    @Test
    fun httpUrlIsLink() {
        val parsed = TextInbound.parse("http://notes.local") as TextPayload.Link
        assertEquals("notes.local", parsed.host)
    }

    @Test
    fun proseAroundUrlIsText() {
        val parsed = TextInbound.parse("read https://example.com tonight") as TextPayload.Prose
        assertEquals(ItemKind.TEXT, parsed.kind)
        assertEquals("read https://example.com tonight", parsed.body)
        assertEquals("read https://example.com tonight", parsed.displayName)
    }

    @Test
    fun bareHostIsText() {
        val parsed = TextInbound.parse("example.com") as TextPayload.Prose
        assertEquals(ItemKind.TEXT, parsed.kind)
    }

    @Test
    fun firstLineIsTitle() {
        val parsed = TextInbound.parse("Pack the bag\n\nDon't forget socks") as TextPayload.Prose
        assertEquals("Pack the bag", parsed.displayName)
        assertTrue(parsed.body.contains("Don't forget socks"))
    }
}
