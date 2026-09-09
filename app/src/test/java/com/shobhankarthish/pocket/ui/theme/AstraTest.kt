package com.shobhankarthish.pocket.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class AstraTest {
    @Test
    fun headerAndEmptyTokensMatchFreeze12() {
        assertEquals(20, Astra.TitleSp)
        assertEquals(28, Astra.TitleLineSp)
        assertEquals(72, Astra.HeaderMinDp)
        assertEquals(136, Astra.EmptyAddWidthDp)
        assertEquals(48, Astra.EmptyAddHeightDp)
        assertEquals(28, Astra.EmptyDockAboveSafeDp)
        assertEquals(8, Astra.EmptyAddHowGapDp)
        assertEquals(16, Astra.EmptyNavIconInsetDp)
        assertEquals(12, Astra.RadiusDp)
        assertEquals(48, Astra.HowToAddMinDp)
        assertEquals(2, Astra.SelectionOutlineDp)
        assertEquals(24, Astra.SelectCircleDp)
        assertEquals(0, Astra.AddHideMs)
        assertEquals(16, Astra.SheetGroupGapDp)
        assertEquals(56, Astra.SheetRowDp)
    }
}
