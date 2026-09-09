package com.shobhankarthish.pocket.ui.shelf

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shobhankarthish.pocket.R
import com.shobhankarthish.pocket.shelf.ByteSizeFormatter
import com.shobhankarthish.pocket.shelf.ShareDecision
import com.shobhankarthish.pocket.shelf.ShareOutcome
import com.shobhankarthish.pocket.shelf.ShareOut
import com.shobhankarthish.pocket.shelf.SharePrep
import com.shobhankarthish.pocket.shelf.ShelfItem
import com.shobhankarthish.pocket.shelf.ShelfMode
import com.shobhankarthish.pocket.ui.motion.ItemEnter
import com.shobhankarthish.pocket.ui.motion.MotionMs
import com.shobhankarthish.pocket.ui.motion.PocketMotion
import com.shobhankarthish.pocket.ui.motion.lightHaptic
import com.shobhankarthish.pocket.ui.motion.rememberPocketMotion
import com.shobhankarthish.pocket.ui.theme.Astra
import java.io.File
import kotlin.math.roundToInt

private val ControlShape = RoundedCornerShape(Astra.RadiusDp.dp)
private val SelectionShape = RoundedCornerShape(Astra.RadiusDp.dp)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ShelfScreen(viewModel: ShelfViewModel) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val mode by viewModel.mode.collectAsStateWithLifecycle()
    val itemCount = items.size
    val empty = itemCount == 0
    val browsing = mode is ShelfMode.Browse
    val howToAddSeen by viewModel.howToAddSeen.collectAsStateWithLifecycle()
    val haptics by viewModel.haptics.collectAsStateWithLifecycle()
    val appearance by viewModel.appearance.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val motion = rememberPocketMotion(haptics)
    var knownIds by remember { mutableStateOf<Set<String>?>(null) }
    var showHowTo by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showAddText by remember { mutableStateOf(false) }
    var addTextDraft by remember { mutableStateOf("") }
    var pendingRemove by remember { mutableStateOf<ShelfItem?>(null) }
    var pendingRemoveSelected by remember { mutableStateOf(false) }
    var barMenu by remember { mutableStateOf(false) }
    var detailItem by remember { mutableStateOf<ShelfItem?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var pendingChoice by remember { mutableStateOf<PendingChoice?>(null) }

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

    fun openAddSheet() {
        showAddSheet = true
    }

    fun launchShare(decision: ShareDecision, pairs: List<Pair<ShelfItem, File>>) {
        if (decision is ShareDecision.SendFiles && decision.mixedMimeWarning) {
            viewModel.note(UserMessage.ShareMixed)
        }
        when (val outcome = ShareOut.execute(context, decision, pairs)) {
            ShareOutcome.Nothing -> viewModel.note(UserMessage.ShareNone)
            is ShareOutcome.Partial -> viewModel.note(
                UserMessage.SharePartial(outcome.shared, outcome.skipped),
            )
            ShareOutcome.Done -> Unit
        }
    }

    fun shareItems(chosen: List<ShelfItem>) {
        if (chosen.isEmpty()) return
        val pairs = chosen.map { item -> item to viewModel.fileFor(item) }
        val prep = ShareOut.prepare(pairs)
        when (val decision = prep.decision) {
            ShareDecision.Nothing -> viewModel.note(UserMessage.ShareNone)
            is ShareDecision.Choose -> pendingChoice = PendingChoice(pairs, prep)
            else -> launchShare(decision, pairs)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.userMessages.collect { message ->
            val text = when (message) {
                UserMessage.Added -> context.getString(R.string.added)
                UserMessage.Unsupported -> context.getString(R.string.unsupported)
                UserMessage.Failed -> context.getString(R.string.ingest_failed)
                is UserMessage.Removed -> context.getString(R.string.removed)
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
                UserMessage.ShareMixed -> context.getString(R.string.share_mixed)
                UserMessage.Copied -> context.getString(R.string.copied)
                is UserMessage.OpenFailed -> message.text
            }
            when (message) {
                is UserMessage.Removed -> {
                    val result = snackbar.showSnackbar(
                        message = text,
                        actionLabel = context.getString(R.string.undo),
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoRemoval(message.token)
                    } else {
                        viewModel.commitRemoval(message.token)
                    }
                }
                is UserMessage.RemovedSome -> {
                    val result = snackbar.showSnackbar(
                        message = text,
                        actionLabel = context.getString(R.string.undo),
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoRemoval(message.token)
                    } else {
                        viewModel.commitRemoval(message.token)
                    }
                }
                else -> snackbar.showSnackbar(text)
            }
        }
    }

    LaunchedEffect(items) {
        knownIds = (knownIds ?: emptySet()) + items.map { it.id }.toSet()
    }

    LaunchedEffect(empty, howToAddSeen, browsing) {
        if (empty && !howToAddSeen && browsing) {
            showHowTo = true
        }
    }

    LaunchedEffect(items, detailItem) {
        val open = detailItem ?: return@LaunchedEffect
        if (items.none { it.id == open.id }) {
            detailItem = null
        }
    }

    BackHandler(enabled = showSettings || detailItem != null || !browsing) {
        when {
            showSettings -> showSettings = false
            detailItem != null -> detailItem = null
            else -> viewModel.exitMode()
        }
    }

    val howToSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val addTextSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val choiceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(Modifier.fillMaxSize()) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            if (detailItem == null && !showSettings) SnackbarHost(snackbar)
        },
        topBar = {
            ShelfAppBar(
                itemCount = itemCount,
                mode = mode,
                menuExpanded = barMenu,
                onMenuChange = { barMenu = it },
                onAdd = ::openAddSheet,
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
                onSettings = {
                    barMenu = false
                    showSettings = true
                },
                onClose = viewModel::exitMode,
                onSelectAll = viewModel::selectAll,
                onDeselect = viewModel::deselectAll,
            )
        },
        bottomBar = {
            val selecting = mode as? ShelfMode.Selecting
            if (selecting != null) {
                SelectionBottomBar(
                    enabled = selecting.ids.isNotEmpty(),
                    onShare = { shareItems(viewModel.selectedInShelfOrder()) },
                    onRemove = { pendingRemoveSelected = true },
                )
            } else if (empty) {
                EmptyShelfDock(
                    onAdd = ::openAddSheet,
                    onHowToAdd = { showHowTo = true },
                )
            }
        },
    ) { inner ->
        if (empty) {
            EmptyShelf(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 16.dp,
                ),
            ) {
                itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                    val animateEnter = remember(item.id) {
                        val known = knownIds
                        known != null && item.id !in known
                    }
                    ItemEnter(
                        animate = animateEnter,
                        motion = motion,
                        modifier = Modifier.animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = motion.spec(MotionMs.Remove),
                            placementSpec = motion.spec(MotionMs.Add),
                        ),
                    ) {
                        ShelfItemRow(
                            item = item,
                            index = index,
                            lastIndex = items.lastIndex,
                            mode = mode,
                            motion = motion,
                            onOpen = { detailItem = item },
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
    }

    if (showHowTo) {
        ModalBottomSheet(
            onDismissRequest = {
                showHowTo = false
                viewModel.markHowToAddSeen()
            },
            sheetState = howToSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null,
        ) {
            Column(
                Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
            ) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Astra.HowToAddMinDp.dp),
                    shape = ControlShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(stringResource(R.string.how_to_add_got_it))
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = addSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp),
            ) {
                Text(
                    text = stringResource(R.string.add_items),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
                QuietSheetRow(
                    label = stringResource(R.string.choose_files),
                    onClick = {
                        showAddSheet = false
                        pickDocument()
                    },
                )
                QuietSheetRow(
                    label = stringResource(R.string.add_text),
                    onClick = {
                        showAddSheet = false
                        showAddText = true
                    },
                )
            }
        }
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
        ModalBottomSheet(
            onDismissRequest = {
                showAddText = false
                addTextDraft = ""
            },
            sheetState = addTextSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
            ) {
                Text(
                    text = stringResource(R.string.add_text_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = addTextDraft,
                    onValueChange = { addTextDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.add_text_hint)) },
                    minLines = 3,
                    shape = ControlShape,
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = {
                            showAddText = false
                            addTextDraft = ""
                        },
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
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
                }
            }
        }
    }

    pendingChoice?.let { choice ->
        val decide = choice.prep.decision as ShareDecision.Choose
        ModalBottomSheet(
            onDismissRequest = { pendingChoice = null },
            sheetState = choiceSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            MixedShareSheet(
                fileCount = decide.fileCount(),
                textCount = decide.textCount(),
                mixedMimeWarning = (decide.files as? ShareDecision.SendFiles)?.mixedMimeWarning == true,
                onShareFiles = {
                    pendingChoice = null
                    launchShare(decide.files, choice.pairs)
                },
                onShareText = {
                    pendingChoice = null
                    launchShare(decide.text, choice.pairs)
                },
                onCopyText = {
                    pendingChoice = null
                    ShareOut.copyText(context, decide.text.body)
                    viewModel.note(UserMessage.Copied)
                },
            )
        }
    }

    detailItem?.let { item ->
        ItemDetail(
            item = item,
            file = viewModel.fileFor(item),
            onClose = { detailItem = null },
            onShare = { shareItems(listOf(item)) },
            onRemove = { pendingRemove = item },
            onCopied = { viewModel.note(UserMessage.Copied) },
            onOpenFailed = { viewModel.note(UserMessage.OpenFailed(it)) },
        )
        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (showSettings) {
        SettingsScreen(
            appearance = appearance,
            haptics = haptics,
            storageLabel = ByteSizeFormatter.format(items.sumOf { it.byteSize }),
            canClear = items.isNotEmpty(),
            onAppearance = viewModel::setAppearance,
            onHaptics = viewModel::setHaptics,
            onClearShelf = viewModel::clearShelf,
            onClose = { showSettings = false },
        )
        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
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
                        if (detailItem?.id == item.id) detailItem = null
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
    }
}

private data class PendingChoice(
    val pairs: List<Pair<ShelfItem, File>>,
    val prep: SharePrep,
)

@Composable
private fun QuietSheetRow(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Astra.SheetRowDp.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    )
}

@Composable
private fun ShelfAppBar(
    itemCount: Int,
    mode: ShelfMode,
    menuExpanded: Boolean,
    onMenuChange: (Boolean) -> Unit,
    onAdd: () -> Unit,
    onHowToAdd: () -> Unit,
    onSelectItems: () -> Unit,
    onArrange: () -> Unit,
    onSettings: () -> Unit,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselect: () -> Unit,
) {
    val selectedCount = (mode as? ShelfMode.Selecting)?.ids?.size ?: 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .heightIn(min = Astra.HeaderMinDp.dp)
            .padding(start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (mode) {
            ShelfMode.Browse -> BrowseBar(
                menuExpanded = menuExpanded,
                itemCount = itemCount,
                onMenuChange = onMenuChange,
                onAdd = onAdd,
                onSelectItems = onSelectItems,
                onArrange = onArrange,
                onHowToAdd = onHowToAdd,
                onSettings = onSettings,
            )
            is ShelfMode.Selecting -> SelectingBar(
                selectedCount = selectedCount,
                allSelected = selectedCount > 0 && selectedCount == itemCount,
                onClose = onClose,
                onSelectAll = onSelectAll,
                onDeselect = onDeselect,
            )
            ShelfMode.Arranging -> ArrangingBar(onDone = onClose)
        }
    }
}

@Composable
private fun RowScope.BrowseBar(
    menuExpanded: Boolean,
    itemCount: Int,
    onMenuChange: (Boolean) -> Unit,
    onAdd: () -> Unit,
    onSelectItems: () -> Unit,
    onArrange: () -> Unit,
    onHowToAdd: () -> Unit,
    onSettings: () -> Unit,
) {
    Column(
        Modifier
            .padding(start = 12.dp)
            .weight(1f),
    ) {
        Text(
            text = stringResource(R.string.shelf_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (itemCount > 0) {
            Text(
                text = pluralStringResource(R.plurals.item_count, itemCount, itemCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    if (itemCount > 0) {
        TextButton(onClick = onAdd) {
            Text(
                text = stringResource(R.string.add),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelLarge,
            )
        }
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
            DropdownMenuItem(
                text = { Text(stringResource(R.string.settings)) },
                onClick = onSettings,
            )
        }
    }
}

@Composable
private fun RowScope.SelectingBar(
    selectedCount: Int,
    allSelected: Boolean,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselect: () -> Unit,
) {
    IconButton(onClick = onClose) {
        Icon(
            Icons.Filled.Close,
            contentDescription = stringResource(R.string.close),
            tint = MaterialTheme.colorScheme.onBackground,
        )
    }
    Text(
        text = pluralStringResource(R.plurals.selected_count, selectedCount, selectedCount),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.weight(1f),
    )
    TextButton(onClick = if (allSelected) onDeselect else onSelectAll) {
        Text(
            text = stringResource(if (allSelected) R.string.deselect else R.string.select_all),
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun RowScope.ArrangingBar(onDone: () -> Unit) {
    Text(
        text = stringResource(R.string.arrange),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .padding(start = 12.dp)
            .weight(1f),
    )
    TextButton(onClick = onDone) {
        Text(
            text = stringResource(R.string.done),
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun MixedShareSheet(
    fileCount: Int,
    textCount: Int,
    mixedMimeWarning: Boolean,
    onShareFiles: () -> Unit,
    onShareText: () -> Unit,
    onCopyText: () -> Unit,
) {
    val actions = listOf(
        stringResource(R.string.share_files, fileCount) to onShareFiles,
        stringResource(R.string.share_text, textCount) to onShareText,
        stringResource(R.string.copy_text) to onCopyText,
    )
    Column(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.share_selected_items),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.share_files_and_text_separately),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        if (mixedMimeWarning) {
            Text(
                text = stringResource(R.string.share_mixed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
        }
        Spacer(Modifier.height(Astra.SheetGroupGapDp.dp))
        actions.forEach { (label, action) ->
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = Astra.SheetRowDp.dp)
                    .clickable(role = Role.Button, onClick = action)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun QuietHairline() {
    val dark = isSystemInDarkTheme()
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
}

@Composable
private fun SelectionBottomBar(
    enabled: Boolean,
    onShare: () -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding(),
    ) {
        QuietHairline()
        val actionColor = MaterialTheme.colorScheme.onBackground.copy(alpha = if (enabled) 1f else 0.38f)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            TextButton(onClick = onShare, enabled = enabled) {
                Text(
                    text = stringResource(R.string.share),
                    color = actionColor,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Text(
                text = "\u00B7",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            TextButton(onClick = onRemove, enabled = enabled) {
                Text(
                    text = stringResource(R.string.remove),
                    color = actionColor,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun EmptyShelf(
    modifier: Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = stringResource(R.string.empty_headline),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyShelfDock(
    onAdd: () -> Unit,
    onHowToAdd: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = Astra.EmptyDockAboveSafeDp.dp,
            ),
        horizontalAlignment = Alignment.Start,
    ) {
        Button(
            onClick = onAdd,
            modifier = Modifier.size(
                width = Astra.EmptyAddWidthDp.dp,
                height = Astra.EmptyAddHeightDp.dp,
            ),
            shape = ControlShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = stringResource(R.string.add_items),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
            )
        }
        TextButton(
            onClick = onHowToAdd,
            modifier = Modifier.heightIn(min = Astra.HowToAddMinDp.dp),
            contentPadding = PaddingValues(horizontal = 0.dp),
        ) {
            Text(
                text = stringResource(R.string.how_to_add),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShelfItemRow(
    item: ShelfItem,
    index: Int,
    lastIndex: Int,
    mode: ShelfMode,
    motion: PocketMotion,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onRemove: () -> Unit,
    onToggle: () -> Unit,
    onLongPress: () -> Unit,
    onMove: (Int) -> Unit,
    onDragTo: (Int) -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    var dragDy by remember { mutableFloatStateOf(0f) }
    val context = LocalContext.current
    val density = LocalDensity.current
    val view = LocalView.current
    val file = remember(item.id) {
        java.io.File(context.filesDir, "shelf/${item.relativePath}")
    }
    val selecting = mode as? ShelfMode.Selecting
    val arranging = mode is ShelfMode.Arranging
    val selected = selecting != null && item.id in selecting.ids
    val lifting = dragDy != 0f
    val boxed = selected || lifting
    val strokeTarget = when {
        selected -> MaterialTheme.colorScheme.onSurface
        else -> Color.Transparent
    }
    val stroke by animateColorAsState(
        targetValue = strokeTarget,
        animationSpec = motion.spec(MotionMs.Selection),
        label = "select-outline",
    )
    val fillTarget = when {
        selected || lifting -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.Transparent
    }
    val container by animateColorAsState(
        targetValue = fillTarget,
        animationSpec = motion.spec(MotionMs.Selection),
        label = "select-fill",
    )
    val rowModifier = Modifier
        .fillMaxWidth()
        .zIndex(if (lifting) 1f else 0f)
        .offset { IntOffset(0, dragDy.roundToInt()) }
        .then(
            if (boxed) {
                Modifier
                    .clip(SelectionShape)
                    .background(container)
                    .border(Astra.SelectionOutlineDp.dp, stroke, SelectionShape)
            } else {
                Modifier.background(container)
            },
        )
        .then(
            when {
                selecting != null -> Modifier.combinedClickable(onClick = onToggle, onLongClick = onToggle)
                arranging -> Modifier
                else -> Modifier.combinedClickable(onClick = onOpen, onLongClick = onLongPress)
            },
        )
        .padding(vertical = 12.dp, horizontal = 4.dp)

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
                            onDragStart = { lightHaptic(view, motion.haptics) },
                            onDragEnd = {
                                dragDy = 0f
                                lightHaptic(view, motion.haptics)
                            },
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
        ItemThumb(
            item = item,
            file = file,
            modifier = if (arranging) Modifier.size(40.dp) else Modifier,
        )
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
                SelectCircle(selected = selected, onClick = onToggle)
            }
            arranging -> {
                CompactMoveButton(
                    up = true,
                    enabled = index > 0,
                    onClick = { onMove(-1) },
                )
                CompactMoveButton(
                    up = false,
                    enabled = index < lastIndex,
                    onClick = { onMove(1) },
                )
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

@Composable
private fun SelectCircle(selected: Boolean, onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .size(Astra.SelectCircleDp.dp)
            .clip(CircleShape)
            .background(if (selected) color else Color.Transparent)
            .border(Astra.SelectionOutlineDp.dp, color, CircleShape)
            .clickable(role = Role.Checkbox, onClick = onClick),
    )
}

@Composable
private fun CompactMoveButton(up: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.38f)
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (up) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = stringResource(if (up) R.string.move_up else R.string.move_down),
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
    }
}
