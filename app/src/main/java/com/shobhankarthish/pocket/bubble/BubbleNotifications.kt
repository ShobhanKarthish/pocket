package com.shobhankarthish.pocket.bubble

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.shobhankarthish.pocket.MainActivity
import com.shobhankarthish.pocket.R

object BubbleNotifications {
    const val CHANNEL_ID = "pocket_bubble"
    const val NOTIFICATION_ID = 42

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.bubble_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.bubble_channel_desc)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    fun build(context: Context, count: Int): Notification {
        ensureChannel(context)
        val open = PendingIntent.getActivity(
            context,
            1,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            pendingFlags(),
        )
        val hide = PendingIntent.getService(
            context,
            2,
            Intent(context, BubbleService::class.java).setAction(BubbleService.ACTION_HIDE),
            pendingFlags(),
        )
        val text = if (count == 0) {
            context.getString(R.string.bubble_notification_empty)
        } else {
            context.resources.getQuantityString(R.plurals.item_count, count, count)
        }
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_pocket)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(text)
            .setContentIntent(open)
            .setOngoing(true)
            .setSilent(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(0, context.getString(R.string.bubble_open_shelf), open)
            .addAction(0, context.getString(R.string.bubble_hide), hide)
            .build()
    }

    private fun pendingFlags(): Int {
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = flags or PendingIntent.FLAG_IMMUTABLE
        }
        return flags
    }
}
