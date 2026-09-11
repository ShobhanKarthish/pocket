package com.shobhankarthish.pocket.bubble

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BubblePositionTest {
    private val box = ScreenBox(width = 1080, height = 1920, left = 40, top = 80, right = 20, bottom = 120)

    @Test
    fun clampStaysInsideUsableArea() {
        val (x, y) = BubblePosition.clamp(-100, -100, childW = 200, childH = 200, box = box)
        assertEquals(40, x)
        assertEquals(80, y)
        val (x2, y2) = BubblePosition.clamp(5000, 5000, childW = 200, childH = 200, box = box)
        assertEquals(860, x2)
        assertEquals(1600, y2)
    }

    @Test
    fun snapChoosesNearestHorizontalEdge() {
        val (leftX, onLeft) = BubblePosition.snapX(100, 200, box)
        assertTrue(onLeft)
        assertEquals(40, leftX)
        val (rightX, onRight) = BubblePosition.snapX(800, 200, box)
        assertFalse(onRight)
        assertEquals(860, rightX)
    }

    @Test
    fun placementSurvivesRotation() {
        val portrait = box
        val saved = BubblePosition.toPlacement(x = 800, y = 900, childW = 168, childH = 168, box = portrait)
        assertFalse(saved.onLeft)
        val landscape = ScreenBox(width = 1920, height = 1080, left = 80, top = 40, right = 120, bottom = 20)
        val (x, y) = BubblePosition.fromPlacement(saved, childW = 168, childH = 168, box = landscape)
        assertEquals(landscape.maxX(168), x)
        val expectedY = landscape.usableTop +
            ((landscape.maxY(168) - landscape.usableTop) * saved.yFraction).toInt()
        assertEquals(expectedY, y)
    }

    @Test
    fun defaultFractionStaysOnScreen() {
        val placement = BubblePlacement(onLeft = false, yFraction = BubbleChrome.DefaultYFraction)
        val (x, y) = BubblePosition.fromPlacement(placement, 168, 168, box)
        assertEquals(box.maxX(168), x)
        assertTrue(y in box.usableTop..box.maxY(168))
    }
}
