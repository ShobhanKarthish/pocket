package com.shobhankarthish.pocket.ui.motion

import android.content.ContentResolver
import android.provider.Settings
import android.view.View
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat

object MotionMs {
    const val Selection = 140
    const val Add = 180
    const val Detail = 200
    const val Sheet = 250
    const val Remove = 180
    const val Fab = 140
}

class PocketMotion(val reduce: Boolean) {
    fun <T> spec(ms: Int): FiniteAnimationSpec<T> =
        if (reduce) snap() else tween(ms, easing = FastOutSlowInEasing)

    fun fadeThroughEnter(): EnterTransition =
        if (reduce) {
            EnterTransition.None
        } else {
            fadeIn(spec(MotionMs.Detail)) + scaleIn(
                initialScale = 0.96f,
                animationSpec = spec(MotionMs.Detail),
            )
        }

    fun fadeThroughExit(): ExitTransition =
        if (reduce) ExitTransition.None else fadeOut(spec(120))

    fun fabEnter(): EnterTransition =
        if (reduce) {
            EnterTransition.None
        } else {
            fadeIn(spec(MotionMs.Fab)) + scaleIn(
                initialScale = 0.92f,
                animationSpec = spec(MotionMs.Fab),
            )
        }

    fun fabExit(): ExitTransition =
        if (reduce) {
            ExitTransition.None
        } else {
            fadeOut(spec(MotionMs.Fab)) + scaleOut(
                targetScale = 0.92f,
                animationSpec = spec(MotionMs.Fab),
            )
        }

    fun barEnter(): EnterTransition =
        if (reduce) {
            EnterTransition.None
        } else {
            fadeIn(spec(MotionMs.Selection)) + slideInVertically(
                animationSpec = spec(MotionMs.Selection),
            ) { it }
        }

    fun barExit(): ExitTransition =
        if (reduce) {
            ExitTransition.None
        } else {
            fadeOut(spec(MotionMs.Selection)) + slideOutVertically(
                animationSpec = spec(MotionMs.Selection),
            ) { it }
        }

    fun checkEnter(): EnterTransition =
        if (reduce) {
            EnterTransition.None
        } else {
            fadeIn(spec(MotionMs.Selection)) + expandHorizontally(animationSpec = spec(MotionMs.Selection))
        }

    fun checkExit(): ExitTransition =
        if (reduce) {
            ExitTransition.None
        } else {
            fadeOut(spec(MotionMs.Selection)) + shrinkHorizontally(animationSpec = spec(MotionMs.Selection))
        }
}

@Composable
fun rememberPocketMotion(): PocketMotion {
    val context = LocalContext.current
    val reduce = remember(context) { animatorOff(context.contentResolver) }
    return remember(reduce) { PocketMotion(reduce) }
}

fun animatorOff(resolver: ContentResolver): Boolean {
    fun scale(key: String): Float =
        Settings.Global.getFloat(resolver, key, 1f)
    return scale(Settings.Global.ANIMATOR_DURATION_SCALE) == 0f ||
        scale(Settings.Global.TRANSITION_ANIMATION_SCALE) == 0f
}

fun lightHaptic(view: View) {
    val enabled = Settings.System.getInt(
        view.context.contentResolver,
        Settings.System.HAPTIC_FEEDBACK_ENABLED,
        1,
    ) != 0
    if (!enabled) return
    ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CLOCK_TICK)
}

@Composable
fun ItemEnter(
    animate: Boolean,
    motion: PocketMotion,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var settled by remember { mutableStateOf(!animate || motion.reduce) }
    LaunchedEffect(Unit) { settled = true }
    val fade by animateFloatAsState(
        targetValue = if (settled) 1f else 0f,
        animationSpec = motion.spec(MotionMs.Add),
        label = "add-fade",
    )
    val settle by animateDpAsState(
        targetValue = if (settled) 0.dp else 8.dp,
        animationSpec = motion.spec(MotionMs.Add),
        label = "add-settle",
    )
    val density = LocalDensity.current
    Box(
        modifier.graphicsLayer {
            alpha = fade
            translationY = with(density) { settle.toPx() }
        },
    ) {
        content()
    }
}

internal val AddSettle: Dp = 8.dp
