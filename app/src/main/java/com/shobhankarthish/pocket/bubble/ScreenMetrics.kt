package com.shobhankarthish.pocket.bubble

import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.view.WindowInsets
import android.view.WindowManager
import kotlin.math.roundToInt

object ScreenMetrics {
    fun box(wm: WindowManager, resources: Resources): ScreenBox {
        val density = resources.displayMetrics.density
        val pad = (BubbleChrome.EdgePadDp * density).roundToInt()
        val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = wm.currentWindowMetrics
            val bounds: Rect = metrics.bounds
            val types = WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
            val insets = metrics.windowInsets.getInsets(types)
            ScreenBox(
                width = bounds.width(),
                height = bounds.height(),
                left = insets.left,
                top = insets.top,
                right = insets.right,
                bottom = insets.bottom,
            )
        } else {
            @Suppress("DEPRECATION")
            val display = wm.defaultDisplay
            val size = android.graphics.Point()
            @Suppress("DEPRECATION")
            display.getRealSize(size)
            ScreenBox(
                width = size.x,
                height = size.y,
                left = 0,
                top = statusBarHeight(resources),
                right = 0,
                bottom = navigationBarHeight(resources),
            )
        }
        return raw.padded(pad)
    }

    fun dp(resources: Resources, dp: Int): Int =
        (dp * resources.displayMetrics.density).roundToInt()

    private fun statusBarHeight(resources: Resources): Int {
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else 0
    }

    private fun navigationBarHeight(resources: Resources): Int {
        val id = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else 0
    }
}
