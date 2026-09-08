package com.shobhankarthish.pocket.shelf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InboundMimesTest {
    @Test
    fun prefersResolverType() {
        assertEquals(
            "image/png",
            InboundMimes.resolve("image/png", "notes.pdf"),
        )
    }

    @Test
    fun stripsCharset() {
        assertEquals(
            "application/pdf",
            InboundMimes.resolve("application/pdf; charset=binary", null),
        )
    }

    @Test
    fun fallsBackToExtensionWhenResolverTypeMissing() {
        assertEquals("application/pdf", InboundMimes.resolve(null, "Assignment.pdf"))
        assertEquals("image/jpeg", InboundMimes.resolve("", "DCIM/shot.JPG"))
        assertEquals("image/png", InboundMimes.resolve(null, "pocket-demo.png"))
    }

    @Test
    fun octetStreamFallsBackToExtension() {
        assertEquals(
            "application/pdf",
            InboundMimes.resolve("application/octet-stream", "Assignment.pdf"),
        )
    }

    @Test
    fun unknownExtensionIsUnsupported() {
        assertNull(InboundMimes.resolve(null, "note.txt"))
        assertNull(InboundMimes.resolve("  ", null))
    }
}
