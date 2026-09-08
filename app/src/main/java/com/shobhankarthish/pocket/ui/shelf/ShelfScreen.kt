package com.shobhankarthish.pocket.ui.shelf

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shobhankarthish.pocket.R
import com.shobhankarthish.pocket.shelf.ShareOutcome
import com.shobhankarthish.pocket.shelf.ShareOut
import com.shobhankarthish.pocket.shelf.ShelfItem
import com.shobhankarthish.pocket.shelf.ShelfMode
import kotlin.math.roundToInt

private val CardShape = RoundedCornerShape(16.dp)
private val FabShape = RoundedCornerShape(16.dp)
private val PillShape = RoundedCornerShape(50)
private val IconWellShape = RoundedCornerShape(12.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelfScreen(viewModel: ShelfViewModel) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val mode by viewModel.mode.collectAsStateWithLifecycle()
    val itemCount = items.size
    val empty = itemCount == 0
    val browsing = mode is ShelfMode.Browse
    val howToAddSeen by viewModel.howToAddSeen.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showHowTo by remember { mutableStateOf(false) }
    var showAddText by remember { mutableStateOf(false) }
    var addTextDraft by remember { mutableStateOf("") }
    var pendingRemove by remember { mutableStateOf<ShelfItem?>(null) }
    var pendingRemoveSelected by remember { mutableStateOf(false) }
    var barMenu by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        contract = object : ActivityResultContracts.OpenDocument() {
            override fun createIntent(context: Context, input: Array<String>): Intent {
                return super.createIntent(context, input).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                }
            }
        },
    ) { uri ->
        uri?.let(viewModel::ingest)
    }

    fun pickDocument() {
        picker.launch(arrayOf("image/*", "application/pdf"))
    }

    fun shareItems(chosen: List<ShelfItem>) {
        if (chosen.isEmpty()) return
        val pairs = chosen.map { item -> item to viewModel.fileFor(item) }
        when (val outcome = ShareOut.sendSelection(context, pairs)) {
            ShareOutcome.Nothing -> viewModel.note(UserMessage.ShareNone)
            is ShareOutcome.Partial -> viewModel.note(
                UserMessage.SharePartial(outcome.shared, outcome.skipped),
            )
            ShareOutcome.Sent -> Unit
        }
    }

    LaunchedEffect(Unit) {
        viewModel.userMessages.collect { message ->
            val text = when (message) {
                UserMessage.Added -> context.getString(R.string.added)
                UserMessage.Unsupported -> context.getString(R.string.unsupported)
                UserMessage.Failed -> context.getString(R.string.ingest_failed)
                UserMessage.Removed -> context.getString(R.string.removed)
                is UserMessage.AddedSome -> context.getString(
                    R.string.added_some,
                    message.added,
                    message.attempted,
                )
                is UserMessage.RemovedSome -> context.getString(R.string.removed_some, message.count)
                is UserMessage.SharePartial -> context.getString(
                    R.string.share_partial,
                    message.shared,
                    message.shared + message.skipped,
                )
                UserMessage.ShareNone -> context.getString(R.string.share_none)
            }
            snackbar.showSnackbar(text)
        }
    }

    LaunchedEffect(empty, howToAddSeen, browsing) {
        if (empty && !howToAddSeen && browsing) {
            showHowTo = true
        }
    }

    BackHandler(enabled = !browsing) {
        viewModel.exitMode()
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            ShelfAppBar(
                itemCount = itemCount,
                mode = mode,
                menuExpanded = barMenu,
                onMenuChange = { barMenu = it },
                onAddText = {
                    barMenu = false
                    showAddText = true
                },
                onHowToAdd = {
                    barMenu = false
                    showHowTo = true
                },
                onSelectItems = {
                    barMenu = false
                    viewModel.enterSelecting()
                },
                onArrange = {
                    barMenu = false
                    viewModel.enterArranging()
                },
                onBack = viewModel::exitMode,
                onShareSelected = { shareItems(viewModel.selectedInShelfOrder()) },
                onRemoveSelected = { pendingRemoveSelected = true },
                onSelectAll = {
                    barMenu = false
                    viewModel.selectAll()
                },
                onDeselect = {
                    barMenu = false
                    viewModel.deselectAll()
                },
            )
        },
        floatingActionButton = {
            if (!empty && browsing) {
                FloatingActionButton(
                    onClick = ::pickDocument,
                    shape = FabShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_items))
                }
            }
        },
    ) { inner ->
        if (empty) {
            EmptyShelf(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                onAdd = ::pickDocument,
                onHowToAdd = { showHowTo = true },
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                    ShelfItemCard(
                        item = item,
                        index = index,
                        lastIndex = items.lastIndex,
                        mode = mode,
                        onShare = {
                            shareItems(listOf(item))
                        },
                        onRemove = { pendingRemove = item },
                        onToggle = { viewModel.toggleSelected(item.id) },
                        onLongPress = { viewModel.enterSelecting(item.id) },
                        onMove = { delta -> viewModel.moveItem(item.id, delta) },
                        onDragTo = { to -> viewModel.moveItemTo(index, to) },
                    )
                }
            }
        }
    }

    if (showHowTo) {
        ModalBottomSheet(
            onDismissRequest = {
                showHowTo = false
                viewModel.markHowToAddSeen()
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(
                    text = stringResource(R.string.how_to_add_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.how_to_add_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        showHowTo = false
                        viewModel.markHowToAddSeen()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(stringResource(R.string.how_to_add_got_it))
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }

    pendingRemove?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingRemove = null },
            title = { Text(stringResource(R.string.remove_title)) },
            text = { Text(stringResource(R.string.remove_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.remove(item)
                        pendingRemove = null
                    },
                ) {
                    Text(stringResource(R.string.remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemove = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }

    if (pendingRemoveSelected) {
        AlertDialog(
            onDismissRequest = { pendingRemoveSelected = false },
            title = { Text(stringResource(R.string.remove_title)) },
            text = { Text(stringResource(R.string.remove_selected_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeSelected()
                        pendingRemoveSelected = false
                    },
                ) {
                    Text(stringResource(R.string.remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemoveSelected = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }

    if (showAddText) {
        AlertDialog(
            onDismissRequest = {
                showAddText = false
                addTextDraft = ""
            },
            title = { Text(stringResource(R.string.add_text_title)) },
            text = {
                OutlinedTextField(
                    value = addTextDraft,
                    onValueChange = { addTextDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.add_text_hint)) },
                    minLines = 3,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val draft = addTextDraft
                        showAddText = false
                        addTextDraft = ""
                        viewModel.ingestText(draft)
                    },
                    enabled = addTextDraft.isNotBlank(),
                ) {
                    Text(stringResource(R.string.add_text_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddText = false
                        addTextDraft = ""
                    },
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

@Composable
private fun ShelfAppBar(
    itemCount: Int,
    mode: ShelfMode,
    menuExpanded: Boolean,
    onMenuChange: (Boolean) -> Unit,
    onAddText: () -> Unit,
    onHowToAdd: () -> Unit,
    onSelectItems: () -> Unit,
    onArrange: () -> Unit,
    onBack: () -> Unit,
    onShareSelected: () -> Unit,
    onRemoveSelected: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselect: () -> Unit,
) {
    val selectedCount = (mode as? ShelfMode.Selecting)?.ids?.size ?: 0
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (mode) {
                ShelfMode.Browse -> BrowseBar(
                    menuExpanded = menuExpanded,
                    itemCount = itemCount,
                    onMenuChange = onMenuChange,
                    onAddText = onAddText,
                    onSelectItems = onSelectItems,
                    onArrange = onArrange,
                    onHowToAdd = onHowToAdd,
                )
                is ShelfMode.Selecting -> SelectingBar(
                    selectedCount = selectedCount,
                    menuExpanded = menuExpanded,
                    onMenuChange = onMenuChange,
                    onBack = onBack,
                    onShareSelected = onShareSelected,
                    onRemoveSelected = onRemoveSelected,
                    onSelectAll = onSelectAll,
                    onDeselect = onDeselect,
                )
                ShelfMode.Arranging -> ArrangingBar(onBack = onBack)
            }
        }
        if (itemCount > 0 && mode is ShelfMode.Browse) {
            Text(
                text = pluralStringResource(R.plurals.item_count, itemCount, itemCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            )
        }
    }
}

@Composable
private fun RowScope.BrowseBar(
    menuExpanded: Boolean,
    itemCount: Int,
    onMenuChange: (Boolean) -> Unit,
    onAddText: () -> Unit,
    onSelectItems: () -> Unit,
    onArrange: () -> Unit,
    onHowToAdd: () -> Unit,
) {
    Text(
        text = stringResource(R.string.shelf_title),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .padding(start = 12.dp)
            .weight(1f),
    )
    Box {
        IconButton(onClick = { onMenuChange(true) }) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.more),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { onMenuChange(false) },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.add_text)) },
                onClick = onAddText,
            )
            if (itemCount > 0) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.select_items)) },
                    onClick = onSelectItems,
                )
            }
            if (itemCount > 1) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.arrange)) },
                    onClick = onArrange,
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.how_to_add)) },
                onClick = onHowToAdd,
            )
        }
    }
}

@Composable
private fun RowScope.SelectingBar(
    selectedCount: Int,
    menuExpanded: Boolean,
    onMenuChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onShareSelected: () -> Unit,
    onRemoveSelected: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselect: () -> Unit,
) {
    IconButton(onClick = onBack) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back),
            tint = MaterialTheme.colorScheme.onBackground,
        )
    }
    Text(
        text = pluralStringResource(R.plurals.selected_count, selectedCount, selectedCount),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.weight(1f),
    )
    IconButton(onClick = onShareSelected, enabled = selectedCount > 0) {
        Icon(
            Icons.Filled.Share,
            contentDescription = stringResource(R.string.share),
            tint = MaterialTheme.colorScheme.onBackground,
        )
    }
    IconButton(onClick = onRemoveSelected, enabled = selectedCount > 0) {
        Icon(
            Icons.Filled.Delete,
            contentDescription = stringResource(R.string.remove),
            tint = MaterialTheme.colorScheme.onBackground,
        )
    }
    Box {
        IconButton(onClick = { onMenuChange(true) }) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.more),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { onMenuChange(false) },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.select_all)) },
                onClick = onSelectAll,
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.deselect)) },
                onClick = onDeselect,
            )
        }
    }
}

@Composable
private fun RowScope.ArrangingBar(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back),
            tint = MaterialTheme.colorScheme.onBackground,
        )
    }
    Text(
        text = stringResource(R.string.arrange),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.weight(1f),
    )
    TextButton(onClick = onBack) {
        Text(
            text = stringResource(R.string.done),
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun EmptyShelf(
    modifier: Modifier,
    onAdd: () -> Unit,
    onHowToAdd: () -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(IconWellShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_shelf),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.empty_headline),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onAdd,
            shape = PillShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp),
        ) {
            Text(
                text = stringResource(R.string.add_items),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
            )
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onHowToAdd) {
            Text(
                text = stringResource(R.string.how_to_add),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShelfItemCard(
    item: ShelfItem,
    index: Int,
    lastIndex: Int,
    mode: ShelfMode,
    onShare: () -> Unit,
    onRemove: () -> Unit,
    onToggle: () -> Unit,
    onLongPress: () -> Unit,
    onMove: (Int) -> Unit,
    onDragTo: (Int) -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    var dragDy by remember { mutableFloatStateOf(0f) }
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val density = LocalDensity.current
    val file = remember(item.id) {
        java.io.File(context.filesDir, "shelf/${item.relativePath}")
    }
    val selecting = mode as? ShelfMode.Selecting
    val arranging = mode is ShelfMode.Arranging
    val selected = selecting != null && item.id in selecting.ids
    val stroke = when {
        selected -> MaterialTheme.colorScheme.onSurface
        dark -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val rowModifier = Modifier
        .fillMaxWidth()
        .zIndex(if (dragDy != 0f) 1f else 0f)
        .offset { IntOffset(0, dragDy.roundToInt()) }
        .clip(CardShape)
        .background(MaterialTheme.colorScheme.surface)
        .border(1.dp, stroke, CardShape)
        .then(
            when {
                selecting != null -> Modifier.combinedClickable(onClick = onToggle, onLongClick = onToggle)
                arranging -> Modifier
                else -> Modifier.combinedClickable(onClick = {}, onLongClick = onLongPress)
            },
        )
        .padding(12.dp)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (arranging) {
            Icon(
                painter = painterResource(R.drawable.ic_drag_handle),
                contentDescription = stringResource(R.string.drag_handle),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(24.dp)
                    .pointerInput(item.id, index, lastIndex) {
                        val step = with(density) { 80.dp.toPx() }
                        detectVerticalDragGestures(
                            onDragEnd = { dragDy = 0f },
                            onDragCancel = { dragDy = 0f },
                        ) { change, dy ->
                            change.consume()
                            dragDy += dy
                            val steps = (dragDy / step).toInt()
                            if (steps != 0) {
                                val target = (index + steps).coerceIn(0, lastIndex)
                                if (target != index) {
                                    onDragTo(target)
                                    dragDy -= steps * step
                                }
                            }
                        }
                    },
            )
            Spacer(Modifier.width(8.dp))
        }
        ItemThumb(item = item, file = file)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = item.titleLine,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.metaLine,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        when {
            selecting != null -> {
                Checkbox(
                    checked = selected,
                    onCheckedChange = { onToggle() },
                )
            }
            arranging -> {
                IconButton(onClick = { onMove(-1) }, enabled = index > 0) {
                    Icon(
                        Icons.Filled.KeyboardArrowUp,
                        contentDescription = stringResource(R.string.move_up),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { onMove(1) }, enabled = index < lastIndex) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.move_down),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> {
                Box {
                    IconButton(onClick = { menu = true }) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.overflow),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.share)) },
                            onClick = {
                                menu = false
                                onShare()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.remove)) },
                            onClick = {
                                menu = false
                                onRemove()
                            },
                        )
                    }
                }
            }
        }
    }
}
