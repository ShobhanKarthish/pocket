package com.shobhankarthish.pocket.shelf

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns

fun ContentResolver.toInboundFile(uri: Uri): InboundFile {
    val displayName = query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }
    return InboundFile(
        mimeType = getType(uri),
        displayName = displayName,
        openStream = { openInputStream(uri) },
    )
}
