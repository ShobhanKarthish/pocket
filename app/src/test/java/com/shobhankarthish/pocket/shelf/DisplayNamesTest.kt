package com.shobhankarthish.pocket.shelf

import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayNamesTest {
    @Test
    fun keepsPlainName() {
        assertEquals("Assignment.pdf", DisplayNames.sanitize("Assignment.pdf", ItemKind.PDF))
    }

    @Test
    fun stripsDirectories() {
        assertEquals("shot.png", DisplayNames.sanitize("/sdcard/DCIM/shot.png", ItemKind.IMAGE))
    }

    @Test
    fun blankFallsBack() {
        assertEquals("document.pdf", DisplayNames.sanitize("  ", ItemKind.PDF))
        assertEquals("image.jpg", DisplayNames.sanitize(null, ItemKind.IMAGE))
    }

    @Test
    fun extensionFromName() {
        assertEquals("png", DisplayNames.extensionOf("Assignment screenshot.png", ItemKind.IMAGE))
        assertEquals("pdf", DisplayNames.extensionOf("notes", ItemKind.PDF))
    }
}
