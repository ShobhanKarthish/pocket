package com.shobhankarthish.pocket.shelf

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import java.io.ByteArrayInputStream

fun ContentResolver.toInboundFile(uri: Uri): InboundFile {
    takeReadGrant(uri)
    val displayName = query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }
    val mimeType = InboundMimes.resolve(getType(uri), displayName)
    val bytes = openInputStream(uri)?.use { it.readBytes() }
        ?: throw SecurityException("no stream for $uri")
    return InboundFile(
        mimeType = mimeType,
        displayName = displayName,
        openStream = { ByteArrayInputStream(bytes) },
    )
}

fun ContentResolver.takeReadGrant(uri: Uri) {
    runCatching {
        takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}
