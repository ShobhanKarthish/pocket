package com.shobhankarthish.pocket.ui.motion

import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat

object MotionMs {
    const val Selection = 140
    const val Add = 180
    const val Remove = 180
    const val Sheet = 250
}

class PocketMotion(val reduce: Boolean, val haptics: Boolean = true) {
    fun <T> spec(ms: Int): FiniteAnimationSpec<T> =
        if (reduce) snap() else tween(ms, easing = FastOutSlowInEasing)
}

@Composable
fun rememberPocketMotion(haptics: Boolean = true): PocketMotion {
    val context = LocalContext.current
    val resolver = context.contentResolver
    var reduce by remember(resolver) { mutableStateOf(animatorOff(resolver)) }
    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                reduce = animatorOff(resolver)
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.TRANSITION_ANIMATION_SCALE),
            false,
            observer,
        )
        reduce = animatorOff(resolver)
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    return remember(reduce, haptics) { PocketMotion(reduce, haptics) }
}

fun animatorOff(resolver: ContentResolver): Boolean {
    fun scale(key: String): Float =
        Settings.Global.getFloat(resolver, key, 1f)
    return scale(Settings.Global.ANIMATOR_DURATION_SCALE) == 0f ||
        scale(Settings.Global.TRANSITION_ANIMATION_SCALE) == 0f
}

fun lightHaptic(view: View, enabled: Boolean = true) {
    if (!enabled) return
    val systemOn = Settings.System.getInt(
        view.context.contentResolver,
        Settings.System.HAPTIC_FEEDBACK_ENABLED,
        1,
    ) != 0
    if (!systemOn) return
    ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CLOCK_TICK)
}

@Composable
fun ItemEnter(
    animate: Boolean,
    motion: PocketMotion,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var shown by remember { mutableStateOf(!animate || motion.reduce) }
    LaunchedEffect(Unit) { shown = true }
    val fade by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = motion.spec(MotionMs.Add),
        label = "add-fade",
    )
    Box(modifier.graphicsLayer { alpha = fade }) {
        content()
    }
}
