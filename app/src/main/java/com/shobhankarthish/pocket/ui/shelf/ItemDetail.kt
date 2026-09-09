package com.shobhankarthish.pocket.ui.shelf

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shobhankarthish.pocket.R
import com.shobhankarthish.pocket.shelf.ItemKind
import com.shobhankarthish.pocket.shelf.ShareOut
import com.shobhankarthish.pocket.shelf.ShelfItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val PreviewPx = 2048

@Composable
fun ItemDetail(
    item: ShelfItem,
    file: File,
    onClose: () -> Unit,
    onShare: () -> Unit,
    onCopied: () -> Unit,
    onOpenFailed: (String) -> Unit,
) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        DetailBar(title = item.titleLine, onClose = onClose)
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when (item.kind) {
                ItemKind.IMAGE -> ImagePreview(file)
                ItemKind.TEXT -> TextPreview(file, item)
                ItemKind.LINK -> LinkPreview(item)
                ItemKind.PDF -> PdfPreview(item)
            }
        }
        DetailActions(
            item = item,
            onShare = onShare,
            onCopy = {
                val body = ShareOut.textBody(item, file)
                ShareOut.copyText(context, body)
                onCopied()
            },
            onOpen = {
                val opened = when (item.kind) {
                    ItemKind.LINK -> ShareOut.openLink(context, item.displayName)
                    ItemKind.PDF -> ShareOut.openPdf(context, item, file)
                    else -> false
                }
                if (!opened) {
                    onOpenFailed(
                        when (item.kind) {
                            ItemKind.PDF -> context.getString(R.string.pdf_no_viewer)
                            else -> context.getString(R.string.open_failed)
                        },
                    )
                }
            },
        )
    }
}

@Composable
private fun DetailBar(title: String, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose) {
            Icon(
                Icons.Filled.Close,
                contentDescription = stringResource(R.string.close),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
        )
    }
}

@Composable
private fun ImagePreview(file: File) {
    val bitmap by produceState<Bitmap?>(initialValue = null, file.path) {
        value = withContext(Dispatchers.IO) { decodeThumb(file, PreviewPx) }
    }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(bitmap) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val next = (scale * zoom).coerceIn(1f, 6f)
                    scale = next
                    offset = if (next == 1f) Offset.Zero else offset + pan
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = stringResource(R.string.thumbnail),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
                contentScale = ContentScale.Fit,
            )
        } else {
            Text(
                text = stringResource(R.string.preview_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TextPreview(file: File, item: ShelfItem) {
    val body by produceState(initialValue = "", file.path, item.id) {
        value = withContext(Dispatchers.IO) { ShareOut.textBody(item, file) }
    }
    Text(
        text = body,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun LinkPreview(item: ShelfItem) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = item.titleLine,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = item.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PdfPreview(item: ShelfItem) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = item.displayName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = item.metaLine,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.pdf_open_hint),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DetailActions(
    item: ShelfItem,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onOpen: () -> Unit,
) {
    val dark = isSystemInDarkTheme()
    val actions = buildList {
        when (item.kind) {
            ItemKind.LINK, ItemKind.PDF -> add(R.string.open to onOpen)
            else -> Unit
        }
        when (item.kind) {
            ItemKind.TEXT, ItemKind.LINK -> add(R.string.copy to onCopy)
            else -> Unit
        }
        add(R.string.share to onShare)
    }
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding(),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    if (dark) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                ),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            actions.forEachIndexed { index, (label, action) ->
                if (index > 0) {
                    Text(
                        text = "\u00B7",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
                TextButton(onClick = action) {
                    Text(
                        text = stringResource(label),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
