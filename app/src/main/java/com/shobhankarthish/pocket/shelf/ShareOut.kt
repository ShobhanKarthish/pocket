package com.shobhankarthish.pocket.shelf

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.shobhankarthish.pocket.R
import java.io.File

sealed interface ShareOutcome {
    data object Nothing : ShareOutcome
    data object Done : ShareOutcome
    data class Partial(val shared: Int, val skipped: Int) : ShareOutcome
}

object ShareOut {
    fun authority(context: Context): String = "${context.packageName}.files"

    fun prepare(pairs: List<Pair<ShelfItem, File>>): SharePrep {
        val filesById = pairs.associate { it.first.id to it.second }
        return ShareBatch.decide(pairs.map { it.first }, { item ->
            filesById[item.id]?.exists() == true
        }, { item ->
            textBody(item, filesById[item.id])
        })
    }

    fun execute(
        context: Context,
        decision: ShareDecision,
        pairs: List<Pair<ShelfItem, File>>,
    ): ShareOutcome {
        val filesById = pairs.associate { it.first.id to it.second }
        val prep = prepare(pairs)
        when (decision) {
            ShareDecision.Nothing -> return ShareOutcome.Nothing
            is ShareDecision.Choose -> return ShareOutcome.Nothing
            is ShareDecision.SendFile -> sendFile(
                context,
                decision.item,
                filesById[decision.item.id] ?: return ShareOutcome.Nothing,
            )
            is ShareDecision.SendFiles -> sendFiles(context, decision, filesById)
            is ShareDecision.SendText -> startChooser(
                context,
                Intent(Intent.ACTION_SEND).apply {
                    type = TextPayload.TEXT_MIME
                    putExtra(Intent.EXTRA_TEXT, decision.body)
                    putExtra(Intent.EXTRA_SUBJECT, decision.subject)
                },
            )
        }
        return if (prep.skipped == 0) {
            ShareOutcome.Done
        } else {
            ShareOutcome.Partial(decision.sharedCount(), prep.skipped)
        }
    }

    fun copyText(context: Context, text: String) {
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.app_name), text))
    }

    fun openLink(context: Context, url: String): Boolean {
        return try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }

    fun openPdf(context: Context, item: ShelfItem, file: File): Boolean {
        if (!file.exists()) return false
        val uri = FileProvider.getUriForFile(context, authority(context), file)
        val view = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            clipData = ClipData.newRawUri(item.displayName, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return try {
            context.startActivity(view)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }

    fun textBody(item: ShelfItem, file: File?): String {
        return if (file != null && file.exists()) file.readText() else item.displayName
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
        decision: ShareDecision.SendFiles,
        filesById: Map<String, File>,
    ) {
        val uris = ArrayList(
            decision.items.map { item ->
                FileProvider.getUriForFile(context, authority(context), filesById.getValue(item.id))
            },
        )
        val clip = ClipData.newRawUri(decision.items.first().displayName, uris.first())
        uris.drop(1).forEach { uri -> clip.addItem(ClipData.Item(uri)) }
        startChooser(
            context,
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = decision.mime
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                clipData = clip
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
        )
    }

    private fun startChooser(context: Context, share: Intent) {
        val chooser = Intent.createChooser(share, context.getString(R.string.share_chooser))
        if (context !is android.app.Activity) {
            share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}

private fun ShareDecision.sharedCount(): Int = when (this) {
    ShareDecision.Nothing -> 0
    is ShareDecision.SendFile -> 1
    is ShareDecision.SendFiles -> items.size
    is ShareDecision.SendText -> items.size
    is ShareDecision.Choose -> files.sharedCount() + text.items.size
}
