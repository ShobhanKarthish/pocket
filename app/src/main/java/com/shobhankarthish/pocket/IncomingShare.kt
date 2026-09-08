package com.shobhankarthish.pocket

import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat

sealed class IncomingShare {
    data class Stream(val uri: Uri) : IncomingShare()
    data class PlainText(val text: String) : IncomingShare()
}

fun peekShare(intent: Intent?): IncomingShare? {
    if (intent?.action != Intent.ACTION_SEND) return null
    val uri = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
    if (uri != null) return IncomingShare.Stream(uri)
    val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim().orEmpty()
    if (text.isNotEmpty()) return IncomingShare.PlainText(text)
    return null
}

fun dropShare(intent: Intent) {
    intent.action = Intent.ACTION_MAIN
    intent.removeExtra(Intent.EXTRA_STREAM)
    intent.removeExtra(Intent.EXTRA_TEXT)
}
