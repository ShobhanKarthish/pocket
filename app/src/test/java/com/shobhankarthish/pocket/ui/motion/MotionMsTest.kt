package com.shobhankarthish.pocket.ui.motion

import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.TweenSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionMsTest {
    @Test
    fun timingsMatchBriefTableOnly() {
        assertTrue(MotionMs.Selection in 120..160)
        assertTrue(MotionMs.Add in 160..200)
        assertTrue(MotionMs.Remove in 160..200)
        assertEquals(250, MotionMs.Sheet)
    }

    @Test
    fun reduceMotionSnaps() {
        val reduced = PocketMotion(reduce = true).spec<Float>(MotionMs.Add)
        val live = PocketMotion(reduce = false).spec<Float>(MotionMs.Add)
        assertTrue(reduced is SnapSpec<*>)
        assertTrue(live is TweenSpec<*>)
        assertEquals(MotionMs.Add, (live as TweenSpec).durationMillis)
    }
}
