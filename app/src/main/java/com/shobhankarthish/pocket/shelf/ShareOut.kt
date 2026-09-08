package com.shobhankarthish.pocket.shelf

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.shobhankarthish.pocket.R
import java.io.File

sealed interface ShareOutcome {
    data object Nothing : ShareOutcome
    data object Sent : ShareOutcome
    data class Partial(val shared: Int, val skipped: Int) : ShareOutcome
}

object ShareOut {
    fun authority(context: Context): String = "${context.packageName}.files"

    fun send(context: Context, item: ShelfItem, file: File) {
        sendSelection(context, listOf(item to file))
    }

    fun sendSelection(context: Context, pairs: List<Pair<ShelfItem, File>>): ShareOutcome {
        val filesById = pairs.associate { it.first.id to it.second }
        val plan = ShareBatch.plan(pairs.map { it.first }) { item ->
            filesById[item.id]?.exists() == true
        }
        if (plan.isEmpty) return ShareOutcome.Nothing

        if (plan.fileItems.isEmpty()) {
            val body = ShareBatch.joinedText(plan.textItems) { item ->
                textBody(item, filesById[item.id])
            }
            startChooser(
                context,
                Intent(Intent.ACTION_SEND).apply {
                    type = TextPayload.TEXT_MIME
                    putExtra(Intent.EXTRA_TEXT, body)
                    putExtra(Intent.EXTRA_SUBJECT, plan.textItems.first().displayName)
                },
            )
        } else if (plan.fileItems.size == 1 && plan.textItems.isEmpty()) {
            sendFile(context, plan.fileItems[0], filesById.getValue(plan.fileItems[0].id))
        } else {
            sendFiles(context, plan, filesById)
        }

        return if (plan.skipped == 0) {
            ShareOutcome.Sent
        } else {
            ShareOutcome.Partial(plan.sharedCount, plan.skipped)
        }
    }

    private fun sendFile(context: Context, item: ShelfItem, file: File) {
        val uri = FileProvider.getUriForFile(context, authority(context), file)
        startChooser(
            context,
            Intent(Intent.ACTION_SEND).apply {
                type = item.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, item.displayName)
                clipData = ClipData.newRawUri(item.displayName, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
        )
    }

    private fun sendFiles(
        context: Context,
        plan: SharePlan,
        filesById: Map<String, File>,
    ) {
        val uris = ArrayList(
            plan.fileItems.map { item ->
                FileProvider.getUriForFile(context, authority(context), filesById.getValue(item.id))
            },
        )
        val clip = ClipData.newRawUri(plan.fileItems.first().displayName, uris.first())
        uris.drop(1).forEach { uri -> clip.addItem(ClipData.Item(uri)) }
        val share = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = ShareBatch.mimeOf(plan.fileItems)
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            if (plan.textItems.isNotEmpty()) {
                putExtra(
                    Intent.EXTRA_TEXT,
                    ShareBatch.joinedText(plan.textItems) { item ->
                        textBody(item, filesById[item.id])
                    },
                )
            }
            clipData = clip
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startChooser(context, share)
    }

    private fun textBody(item: ShelfItem, file: File?): String {
        return if (file != null && file.exists()) file.readText() else item.displayName
    }

    private fun startChooser(context: Context, share: Intent) {
        context.startActivity(Intent.createChooser(share, context.getString(R.string.share_chooser)))
    }
}
