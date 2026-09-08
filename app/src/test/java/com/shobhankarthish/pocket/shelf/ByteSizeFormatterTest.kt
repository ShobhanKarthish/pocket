package com.shobhankarthish.pocket.shelf

import org.junit.Assert.assertEquals
import org.junit.Test

class ByteSizeFormatterTest {
    @Test
    fun bytes() {
        assertEquals("512 B", ByteSizeFormatter.format(512))
    }

    @Test
    fun kilobytes() {
        assertEquals("420 KB", ByteSizeFormatter.format(420L * 1024))
    }

    @Test
    fun megabytes() {
        assertEquals("1.8 MB", ByteSizeFormatter.format((1.8 * 1024 * 1024).toLong()))
    }

    @Test
    fun negativeBecomesZero() {
        assertEquals("0 B", ByteSizeFormatter.format(-3))
    }
}
