package com.shobhankarthish.pocket.bubble

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shobhankarthish.pocket.R
import com.shobhankarthish.pocket.ui.motion.lightHaptic
import com.shobhankarthish.pocket.ui.theme.Astra
import com.shobhankarthish.pocket.ui.theme.PocketTheme
import kotlinx.coroutines.flow.StateFlow

enum class BubbleMode {
    Collapsed,
    Actions,
    DropTarget,
    ConfirmClear,
}

data class BubbleUiState(
    val mode: BubbleMode = BubbleMode.Collapsed,
    val count: Int = 0,
    val dark: Boolean = false,
    val haptics: Boolean = true,
    val dropHot: Boolean = false,
    val status: String? = null,
)

@Composable
fun BubbleOverlayRoot(
    mode: StateFlow<BubbleMode>,
    count: StateFlow<Int>,
    dark: StateFlow<Boolean>,
    haptics: StateFlow<Boolean>,
    dropHot: StateFlow<Boolean>,
    status: StateFlow<String?>,
    onTap: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    onOpenShelf: () -> Unit,
    onShareAll: () -> Unit,
    onAskClear: () -> Unit,
    onConfirmClear: () -> Unit,
    onCancelClear: () -> Unit,
    onHide: () -> Unit,
) {
    val currentMode by mode.collectAsStateWithLifecycle()
    val currentCount by count.collectAsStateWithLifecycle()
    val currentDark by dark.collectAsStateWithLifecycle()
    val currentHaptics by haptics.collectAsStateWithLifecycle()
    val currentHot by dropHot.collectAsStateWithLifecycle()
    val currentStatus by status.collectAsStateWithLifecycle()
    BubbleOverlay(
        state = BubbleUiState(
            mode = currentMode,
            count = currentCount,
            dark = currentDark,
            haptics = currentHaptics,
            dropHot = currentHot,
            status = currentStatus,
        ),
        onTap = onTap,
        onDrag = onDrag,
        onDragEnd = onDragEnd,
        onOpenShelf = onOpenShelf,
        onShareAll = onShareAll,
        onAskClear = onAskClear,
        onConfirmClear = onConfirmClear,
        onCancelClear = onCancelClear,
        onHide = onHide,
    )
}

@Composable
fun BubbleOverlay(
    state: BubbleUiState,
    onTap: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    onOpenShelf: () -> Unit,
    onShareAll: () -> Unit,
    onAskClear: () -> Unit,
    onConfirmClear: () -> Unit,
    onCancelClear: () -> Unit,
    onHide: () -> Unit,
) {
    PocketTheme(darkTheme = state.dark) {
        when (state.mode) {
            BubbleMode.Collapsed -> CollapsedBubble(
                count = state.count,
                status = state.status,
                haptics = state.haptics,
                onTap = onTap,
                onDrag = onDrag,
                onDragEnd = onDragEnd,
            )
            BubbleMode.DropTarget -> DropShelf(hot = state.dropHot)
            BubbleMode.Actions -> ActionPanel(
                count = state.count,
                status = state.status,
                onOpenShelf = onOpenShelf,
                onShareAll = onShareAll,
                onAskClear = onAskClear,
                onHide = onHide,
            )
            BubbleMode.ConfirmClear -> ConfirmClearPanel(
                onConfirm = onConfirmClear,
                onCancel = onCancelClear,
            )
        }
    }
}

@Composable
private fun CollapsedBubble(
    count: Int,
    status: String?,
    haptics: Boolean,
    onTap: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
) {
    val view = LocalView.current
    val label = stringResource(R.string.bubble_label, count)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.pointerInput(haptics) {
            val slop = view.context.resources.displayMetrics.density * 8f
            awaitEachGesture {
                val down = awaitFirstDown()
                var dragged = false
                var total = Offset.Zero
                drag(down.id) { change ->
                    val delta = change.positionChange()
                    total += delta
                    if (!dragged && total.getDistance() > slop) {
                        dragged = true
                        lightHaptic(view, haptics)
                    }
                    if (dragged) {
                        change.consume()
                        onDrag(delta.x, delta.y)
                    }
                }
                if (dragged) {
                    onDragEnd()
                    lightHaptic(view, haptics)
                } else {
                    lightHaptic(view, haptics)
                    onTap()
                }
            }
        },
    ) {
        Box(
            modifier = Modifier
                .size(BubbleChrome.HitDp.dp)
                .semantics { contentDescription = label },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(BubbleChrome.SizeDp.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_shelf),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
            if (count > 0) {
                val badge = if (count > 9) "9+" else count.toString()
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(BubbleChrome.BadgeDp.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = badge,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 10.sp,
                    )
                }
            }
        }
        if (status != null) {
            Text(
                text = status,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .width(160.dp),
            )
        }
    }
}

@Composable
private fun DropShelf(hot: Boolean) {
    val stroke = if (hot) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.outline
    }
    val fill = if (hot) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }
    Box(
        modifier = Modifier
            .width(BubbleChrome.PanelWidthDp.dp)
            .height(BubbleChrome.DropHeightDp.dp)
            .clip(RoundedCornerShape(Astra.RadiusDp.dp))
            .background(fill)
            .border(Astra.SelectionOutlineDp.dp, stroke, RoundedCornerShape(Astra.RadiusDp.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.bubble_drop_here),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ActionPanel(
    count: Int,
    status: String?,
    onOpenShelf: () -> Unit,
    onShareAll: () -> Unit,
    onAskClear: () -> Unit,
    onHide: () -> Unit,
) {
    val shape = RoundedCornerShape(Astra.RadiusDp.dp)
    Column(
        modifier = Modifier
            .width(BubbleChrome.PanelWidthDp.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Text(
            text = if (count == 0) {
                stringResource(R.string.bubble_empty)
            } else {
                pluralStringResource(R.plurals.item_count, count, count)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        )
        if (status != null) {
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        OverlayRow(stringResource(R.string.bubble_open_shelf), onOpenShelf)
        OverlayRow(
            label = stringResource(R.string.bubble_share_all),
            onClick = onShareAll,
            enabled = count > 0,
        )
        OverlayRow(
            label = stringResource(R.string.bubble_clear),
            onClick = onAskClear,
            enabled = count > 0,
        )
        OverlayRow(stringResource(R.string.bubble_hide), onHide)
    }
}

@Composable
private fun ConfirmClearPanel(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val shape = RoundedCornerShape(Astra.RadiusDp.dp)
    Column(
        modifier = Modifier
            .width(BubbleChrome.PanelWidthDp.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Text(
            text = stringResource(R.string.remove_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.remove_selected_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onCancel, shape = RoundedCornerShape(Astra.RadiusDp.dp)) {
                Text(stringResource(R.string.cancel))
            }
            TextButton(onClick = onConfirm, shape = RoundedCornerShape(Astra.RadiusDp.dp)) {
                Text(stringResource(R.string.clear_shelf))
            }
        }
    }
}

@Composable
private fun OverlayRow(label: String, onClick: () -> Unit, enabled: Boolean = true) {
    val color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (enabled) 1f else 0.38f)
    Text(
        text = label,
        style = MaterialTheme.typography.titleMedium,
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Astra.SheetRowDp.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    )
}
