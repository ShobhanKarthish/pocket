package com.shobhankarthish.pocket.ui.motion

import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.TweenSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionMsTest {
    @Test
    fun reduceMotionSnaps() {
        val reduced = PocketMotion(reduce = true).spec<Float>(MotionMs.Add)
        val live = PocketMotion(reduce = false).spec<Float>(MotionMs.Add)
        assertTrue(reduced is SnapSpec<*>)
        assertTrue(live is TweenSpec<*>)
        assertEquals(MotionMs.Add, (live as TweenSpec).durationMillis)
    }
}
