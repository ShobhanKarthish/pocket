package com.shobhankarthish.pocket.ui.shelf

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shobhankarthish.pocket.R
import com.shobhankarthish.pocket.shelf.ItemKind
import com.shobhankarthish.pocket.shelf.ShelfItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max

private val ThumbShape = RoundedCornerShape(12.dp)
private const val DecodePx = 192
private val Slot = 64.dp

@Composable
fun ItemThumb(
    item: ShelfItem,
    file: File,
    modifier: Modifier = Modifier,
) {
    val slot = Modifier.size(Slot).then(modifier)
    when (item.kind) {
        ItemKind.IMAGE -> {
            val bitmap by produceState<Bitmap?>(initialValue = null, item.id) {
                value = withContext(Dispatchers.IO) { decodeThumb(file, DecodePx) }
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = stringResource(R.string.thumbnail),
                    modifier = slot.clip(ThumbShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                GlyphThumb(modifier = slot, painter = painterResource(R.drawable.ic_shelf))
            }
        }
        ItemKind.PDF -> {
            Box(modifier = slot, contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_pdf),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = "PDF",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp),
                )
            }
        }
        ItemKind.TEXT -> GlyphThumb(
            modifier = slot,
            painter = painterResource(R.drawable.ic_text),
        )
        ItemKind.LINK -> GlyphThumb(
            modifier = slot,
            painter = painterResource(R.drawable.ic_link),
        )
    }
}

@Composable
private fun GlyphThumb(
    modifier: Modifier,
    painter: androidx.compose.ui.graphics.painter.Painter,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(28.dp),
        )
    }
}

fun decodeThumb(file: File, maxPx: Int): Bitmap? {
    if (!file.exists()) return null
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        val w = bounds.outWidth
        val h = bounds.outHeight
        if (w <= 0 || h <= 0) return null
        val sample = max(1, Integer.highestOneBit(max(w, h) / maxPx))
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        BitmapFactory.decodeFile(file.absolutePath, opts)
    } catch (_: Exception) {
        null
    }
}
