package com.shobhankarthish.pocket.bubble

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object OverlayPermission {
    fun canDraw(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun settingsIntent(context: Context): Intent {
        val targeted = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}"),
        )
        return if (targeted.resolveActivity(context.packageManager) != null) {
            targeted
        } else {
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        }
    }

    fun openSettings(context: Context) {
        val first = settingsIntent(context).apply {
            if (context !is android.app.Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        try {
            context.startActivity(first)
        } catch (_: ActivityNotFoundException) {
            val fallback = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                if (context !is android.app.Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(fallback)
        }
    }
}
