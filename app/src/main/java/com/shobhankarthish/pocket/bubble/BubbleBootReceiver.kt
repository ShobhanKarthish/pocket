package com.shobhankarthish.pocket.bubble

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BubbleBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            return
        }
        val pending = goAsync()
        val app = context.applicationContext
        BubbleService.syncInBackground(app) { pending.finish() }
    }
}
