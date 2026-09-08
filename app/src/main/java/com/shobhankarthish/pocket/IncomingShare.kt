package com.shobhankarthish.pocket

import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat

sealed class IncomingShare {
    data class Stream(val uri: Uri) : IncomingShare()
    data class Streams(val uris: List<Uri>) : IncomingShare()
    data class PlainText(val text: String) : IncomingShare()
}

enum class ShareIntake {
    SingleStream,
    MultipleStreams,
    PlainText,
}

fun classifyShare(action: String?, streamCount: Int, hasText: Boolean): ShareIntake? {
    return when (action) {
        Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE -> when {
            streamCount > 1 -> ShareIntake.MultipleStreams
            streamCount == 1 -> ShareIntake.SingleStream
            hasText -> ShareIntake.PlainText
            else -> null
        }
        else -> null
    }
}

fun peekShare(intent: Intent?): IncomingShare? {
    if (intent == null) return null
    val listed = IntentCompat.getParcelableArrayListExtra(
        intent,
        Intent.EXTRA_STREAM,
        Uri::class.java,
    )?.filterNotNull().orEmpty()
    val single = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
    val streams = when {
        listed.isNotEmpty() -> listed
        single != null -> listOf(single)
        else -> emptyList()
    }
    val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim().orEmpty()
    return when (classifyShare(intent.action, streams.size, text.isNotEmpty())) {
        ShareIntake.MultipleStreams -> IncomingShare.Streams(streams)
        ShareIntake.SingleStream -> IncomingShare.Stream(streams.first())
        ShareIntake.PlainText -> IncomingShare.PlainText(text)
        null -> null
    }
}

fun dropShare(intent: Intent) {
    intent.action = Intent.ACTION_MAIN
    intent.removeExtra(Intent.EXTRA_STREAM)
    intent.removeExtra(Intent.EXTRA_TEXT)
}
