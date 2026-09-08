package com.shobhankarthish.pocket.shelf

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.shobhankarthish.pocket.R
import java.io.File

object ShareOut {
    fun authority(context: Context): String = "${context.packageName}.files"

    fun send(context: Context, item: ShelfItem, file: File) {
        if (item.kind == ItemKind.TEXT || item.kind == ItemKind.LINK) {
            val body = if (file.exists()) file.readText() else item.displayName
            val share = Intent(Intent.ACTION_SEND).apply {
                type = TextPayload.TEXT_MIME
                putExtra(Intent.EXTRA_TEXT, body)
                putExtra(Intent.EXTRA_SUBJECT, item.displayName)
            }
            context.startActivity(Intent.createChooser(share, context.getString(R.string.share_chooser)))
            return
        }
        val uri = FileProvider.getUriForFile(context, authority(context), file)
        val share = Intent(Intent.ACTION_SEND).apply {
            type = item.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, item.displayName)
            clipData = ClipData.newRawUri(item.displayName, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(share, context.getString(R.string.share_chooser)))
    }
}
