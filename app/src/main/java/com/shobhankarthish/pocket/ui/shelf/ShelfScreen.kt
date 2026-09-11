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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.SheetState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
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
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private val ControlShape = RoundedCornerShape(Astra.RadiusDp.dp)
private val SelectionShape = RoundedCornerShape(Astra.RadiusDp.dp)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ShelfScreen(
    viewModel: ShelfViewModel,
    overlayAllowed: Boolean,
    onAllowOverlay: () -> Unit,
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val mode by viewModel.mode.collectAsStateWithLifecycle()
    val itemCount = items.size
    val empty = itemCount == 0
    val browsing = mode is ShelfMode.Browse
    val howToAddSeen by viewModel.howToAddSeen.collectAsStateWithLifecycle()
    val haptics by viewModel.haptics.collectAsStateWithLifecycle()
    val appearance by viewModel.appearance.collectAsStateWithLifecycle()
    val bubbleEnabled by viewModel.bubbleEnabled.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val motion = rememberPocketMotion(haptics)
    var knownIds by remember { mutableStateOf<Set<String>?>(null) }
    var showHowTo by rememberSaveable { mutableStateOf(false) }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var showAddText by rememberSaveable { mutableStateOf(false) }
    var addTextDraft by rememberSaveable { mutableStateOf("") }
    var pendingRemove by remember { mutableStateOf<ShelfItem?>(null) }
    var pendingRemoveSelected by remember { mutableStateOf(false) }
    var barMenu by remember { mutableStateOf(false) }
    var detailItem by remember { mutableStateOf<ShelfItem?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var pendingChoice by remember { mutableStateOf<PendingChoice?>(null) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var closingSheet by remember { mutableStateOf(false) }
    var draggingId by remember { mutableStateOf<String?>(null) }

    fun closeSheet(state: SheetState, onClosed: () -> Unit) {
        if (closingSheet) return
        closingSheet = true
        scope.launch {
            try {
                state.hide()
                if (!state.isVisible) onClosed()
            } finally {
                closingSheet = false
            }
        }
    }

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
            }
        },
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner),
        ) {
            if (bubbleEnabled && !overlayAllowed && browsing) {
                Text(
                    text = stringResource(R.string.overlay_denied_banner),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onAllowOverlay)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
            if (empty) {
                EmptyShelf(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
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
                        modifier = Modifier
                            .zIndex(if (draggingId == item.id) 1f else 0f)
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = motion.spec(MotionMs.Remove),
                                placementSpec = if (draggingId == item.id) null else motion.spec(MotionMs.Add),
                            ),
                    ) {
                        ShelfItemRow(
                            item = item,
                            index = index,
                            lastIndex = items.lastIndex,
                            mode = mode,
                            motion = motion,
                            listState = listState,
                            onOpen = { detailItem = item },
                            onShare = {
                                shareItems(listOf(item))
                            },
                            onRemove = { pendingRemove = item },
                            onToggle = { viewModel.toggleSelected(item.id) },
                            onLongPress = { viewModel.enterSelecting(item.id) },
                            onMove = { delta -> viewModel.moveItem(item.id, delta) },
                            onDragTo = viewModel::moveItemTo,
                            onDraggingChange = { draggingId = if (it) item.id else null },
                        )
                    }
                }
            }
        }
        }
    }

    if (empty && detailItem == null && !showSettings) {
        EmptyShelfDock(
            modifier = Modifier.align(Alignment.BottomStart),
            onAdd = ::openAddSheet,
            onHowToAdd = { showHowTo = true },
        )
    }

    if (showHowTo) {
        ModalBottomSheet(
            onDismissRequest = {
                if (!closingSheet) {
                    showHowTo = false
                    viewModel.markHowToAddSeen()
                }
            },
            sheetState = howToSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null,
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
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
                        closeSheet(howToSheetState) {
                            showHowTo = false
                            viewModel.markHowToAddSeen()
                        }
                    },
                    enabled = !closingSheet,
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
            onDismissRequest = { if (!closingSheet) showAddSheet = false },
            sheetState = addSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 20.dp),
            ) {
                Text(
                    text = stringResource(R.string.add_items),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Spacer(Modifier.height(12.dp))
                QuietSheetRow(
                    label = stringResource(R.string.choose_files),
                    onClick = {
                        closeSheet(addSheetState) {
                            showAddSheet = false
                            pickDocument()
                        }
                    },
                )
                QuietSheetRow(
                    label = stringResource(R.string.add_text),
                    onClick = {
                        closeSheet(addSheetState) {
                            showAddSheet = false
                            showAddText = true
                        }
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
                    shape = ControlShape,
                    onClick = {
                        viewModel.removeSelected()
                        pendingRemoveSelected = false
                    },
                ) {
                    Text(stringResource(R.string.remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemoveSelected = false }, shape = ControlShape) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }

    if (showAddText) {
        ModalBottomSheet(
            onDismissRequest = {
                if (!closingSheet) {
                    showAddText = false
                    addTextDraft = ""
                }
            },
            sheetState = addTextSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
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
                    maxLines = 6,
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
                            closeSheet(addTextSheetState) {
                                showAddText = false
                                addTextDraft = ""
                            }
                        },
                        enabled = !closingSheet,
                        shape = ControlShape,
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(
                        onClick = {
                            val draft = addTextDraft
                            closeSheet(addTextSheetState) {
                                showAddText = false
                                addTextDraft = ""
                                viewModel.ingestText(draft)
                            }
                        },
                        enabled = addTextDraft.isNotBlank() && !closingSheet,
                        shape = ControlShape,
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
            onDismissRequest = { if (!closingSheet) pendingChoice = null },
            sheetState = choiceSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null,
        ) {
            MixedShareSheet(
                fileCount = decide.fileCount(),
                textCount = decide.textCount(),
                mixedMimeWarning = (decide.files as? ShareDecision.SendFiles)?.mixedMimeWarning == true,
                onShareFiles = {
                    closeSheet(choiceSheetState) {
                        pendingChoice = null
                        launchShare(decide.files, choice.pairs)
                    }
                },
                onShareText = {
                    closeSheet(choiceSheetState) {
                        pendingChoice = null
                        launchShare(decide.text, choice.pairs)
                    }
                },
                onCopyText = {
                    closeSheet(choiceSheetState) {
                        pendingChoice = null
                        ShareOut.copyText(context, decide.text.body)
                        viewModel.note(UserMessage.Copied)
                    }
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
            bubbleEnabled = bubbleEnabled,
            overlayAllowed = overlayAllowed,
            storageLabel = ByteSizeFormatter.format(items.sumOf { it.byteSize }),
            canClear = items.isNotEmpty(),
            onAppearance = viewModel::setAppearance,
            onHaptics = viewModel::setHaptics,
            onBubbleEnabled = viewModel::setBubbleEnabled,
            onAllowOverlay = onAllowOverlay,
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
                    shape = ControlShape,
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
                TextButton(onClick = { pendingRemove = null }, shape = ControlShape) {
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
        TextButton(onClick = onAdd, shape = ControlShape) {
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
    TextButton(onClick = if (allSelected) onDeselect else onSelectAll, shape = ControlShape) {
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
    TextButton(onClick = onDone, shape = ControlShape) {
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
            .verticalScroll(rememberScrollState())
            .padding(vertical = 20.dp),
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
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
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
            TextButton(onClick = onShare, enabled = enabled, shape = ControlShape) {
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
            TextButton(onClick = onRemove, enabled = enabled, shape = ControlShape) {
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
    modifier: Modifier,
    onAdd: () -> Unit,
    onHowToAdd: () -> Unit,
) {
    Column(
        modifier = modifier
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
        Spacer(Modifier.height(Astra.EmptyAddHowGapDp.dp))
        TextButton(
            onClick = onHowToAdd,
            modifier = Modifier.heightIn(min = Astra.HowToAddMinDp.dp),
            shape = ControlShape,
            contentPadding = PaddingValues(horizontal = 0.dp),
        ) {
            Text(
                text = stringResource(R.string.how_to_add),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelLarge,
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
    listState: LazyListState,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onRemove: () -> Unit,
    onToggle: () -> Unit,
    onLongPress: () -> Unit,
    onMove: (Int) -> Unit,
    onDragTo: (Int, Int) -> Unit,
    onDraggingChange: (Boolean) -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    var dragging by remember { mutableStateOf(false) }
    var dragStartOffset by remember { mutableFloatStateOf(0f) }
    var dragDistance by remember { mutableFloatStateOf(0f) }
    val currentDragTo by rememberUpdatedState(onDragTo)
    val currentDraggingChange by rememberUpdatedState(onDraggingChange)
    val currentMotion by rememberUpdatedState(motion)
    val context = LocalContext.current
    val view = LocalView.current
    val file = remember(item.id) {
        java.io.File(context.filesDir, "shelf/${item.relativePath}")
    }
    val selecting = mode as? ShelfMode.Selecting
    val arranging = mode is ShelfMode.Arranging
    val selected = selecting != null && item.id in selecting.ids
    val lifting = dragging
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
        .offset {
            val currentOffset = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == item.id }?.offset
            val displacement = if (dragging && currentOffset != null) {
                dragStartOffset + dragDistance - currentOffset
            } else {
                0f
            }
            IntOffset(0, displacement.roundToInt())
        }
        .clip(SelectionShape)
        .background(container)
        .border(Astra.SelectionOutlineDp.dp, stroke, SelectionShape)
        .then(
            when {
                selecting != null -> Modifier.toggleable(
                    value = selected,
                    role = Role.Checkbox,
                    onValueChange = { onToggle() },
                )
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
            val moveUpLabel = stringResource(R.string.move_up)
            val moveDownLabel = stringResource(R.string.move_down)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .semantics(mergeDescendants = true) {
                        customActions = buildList {
                            if (index > 0) {
                                add(CustomAccessibilityAction(moveUpLabel) { onMove(-1); true })
                            }
                            if (index < lastIndex) {
                                add(CustomAccessibilityAction(moveDownLabel) { onMove(1); true })
                            }
                        }
                    }
                    .pointerInput(item.id, listState) {
                        var pendingTarget: Int? = null
                        detectVerticalDragGestures(
                            onDragStart = {
                                val info = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == item.id }
                                if (info != null) {
                                    dragStartOffset = info.offset.toFloat()
                                    dragDistance = 0f
                                    pendingTarget = null
                                    dragging = true
                                    currentDraggingChange(true)
                                    lightHaptic(view, currentMotion.haptics)
                                }
                            },
                            onDragEnd = {
                                dragging = false
                                currentDraggingChange(false)
                                lightHaptic(view, currentMotion.haptics)
                            },
                            onDragCancel = {
                                dragging = false
                                currentDraggingChange(false)
                            },
                        ) { change, dy ->
                            change.consume()
                            if (!dragging) return@detectVerticalDragGestures
                            dragDistance += dy
                            val visible = listState.layoutInfo.visibleItemsInfo
                            val current = visible.firstOrNull { it.key == item.id }
                                ?: return@detectVerticalDragGestures
                            // Wait for an in-flight reorder before issuing another index-based move.
                            if (pendingTarget != null && current.index != pendingTarget) {
                                return@detectVerticalDragGestures
                            }
                            pendingTarget = null
                            val center = dragStartOffset + dragDistance + current.size / 2f
                            val target = visible.minByOrNull { abs(it.offset + it.size / 2f - center) }
                            if (target != null && target.index != current.index) {
                                pendingTarget = target.index
                                currentDragTo(current.index, target.index)
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_drag_handle),
                    contentDescription = stringResource(R.string.drag_handle),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
            }
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
                SelectCircle(selected = selected)
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
private fun SelectCircle(selected: Boolean) {
    val color = MaterialTheme.colorScheme.onSurface
    // The whole row owns the checkbox action and state; keep one accessible target.
    Box(
        modifier = Modifier.size(48.dp).clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(Astra.SelectCircleDp.dp)
                .clip(CircleShape)
                .background(if (selected) color else Color.Transparent)
                .border(Astra.SelectionOutlineDp.dp, color, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun CompactMoveButton(up: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.38f)
    Box(
        modifier = Modifier
            .size(48.dp)
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
