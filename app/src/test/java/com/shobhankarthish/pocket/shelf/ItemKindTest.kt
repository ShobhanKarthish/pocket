package com.shobhankarthish.pocket.shelf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ItemKindTest {
    @Test
    fun pngIsImage() {
        assertEquals(ItemKind.IMAGE, ItemKind.fromMime("image/png"))
    }

    @Test
    fun jpegWithParamsIsImage() {
        assertEquals(ItemKind.IMAGE, ItemKind.fromMime("image/jpeg; charset=utf-8"))
    }

    @Test
    fun pdfIsPdf() {
        assertEquals(ItemKind.PDF, ItemKind.fromMime("application/pdf"))
    }

    @Test
    fun pdfCaseInsensitive() {
        assertEquals(ItemKind.PDF, ItemKind.fromMime("Application/PDF"))
    }

    @Test
    fun textIsRejected() {
        assertNull(ItemKind.fromMime("text/plain"))
    }

    @Test
    fun bareImagePrefixIsRejected() {
        assertNull(ItemKind.fromMime("image/"))
    }

    @Test
    fun nullIsRejected() {
        assertNull(ItemKind.fromMime(null))
    }
}
