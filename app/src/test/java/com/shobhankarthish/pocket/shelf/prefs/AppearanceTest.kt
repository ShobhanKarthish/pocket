package com.shobhankarthish.pocket.shelf.prefs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppearanceTest {
    @Test
    fun storeRoundTripAndFallback() {
        assertEquals(Appearance.System, Appearance.fromStore(null))
        assertEquals(Appearance.System, Appearance.fromStore("nope"))
        assertEquals(Appearance.Light, Appearance.fromStore("light"))
        assertEquals(Appearance.Dark, Appearance.fromStore("Dark"))
    }

    @Test
    fun isDarkFollowsChoice() {
        assertTrue(Appearance.System.isDark(systemDark = true))
        assertFalse(Appearance.System.isDark(systemDark = false))
        assertFalse(Appearance.Light.isDark(systemDark = true))
        assertTrue(Appearance.Dark.isDark(systemDark = false))
    }
}
