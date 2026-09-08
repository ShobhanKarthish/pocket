package com.shobhankarthish.pocket.ui.shelf

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shobhankarthish.pocket.R
import com.shobhankarthish.pocket.shelf.ItemKind
import com.shobhankarthish.pocket.shelf.ShareOut
import com.shobhankarthish.pocket.shelf.ShelfItem

private val CardShape = RoundedCornerShape(16.dp)
private val FabShape = RoundedCornerShape(16.dp)
private val PillShape = RoundedCornerShape(50)
private val IconWellShape = RoundedCornerShape(12.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelfScreen(viewModel: ShelfViewModel) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val itemCount = items.size
    val empty = itemCount == 0
    val howToAddSeen by viewModel.howToAddSeen.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showHowTo by remember { mutableStateOf(false) }
    var showAddText by remember { mutableStateOf(false) }
    var addTextDraft by remember { mutableStateOf("") }
    var pendingRemove by remember { mutableStateOf<ShelfItem?>(null) }
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

    LaunchedEffect(Unit) {
        viewModel.userMessages.collect { message ->
            val text = when (message) {
                UserMessage.Added -> context.getString(R.string.added)
                UserMessage.Unsupported -> context.getString(R.string.unsupported)
                UserMessage.Failed -> context.getString(R.string.ingest_failed)
                UserMessage.Removed -> context.getString(R.string.removed)
            }
            snackbar.showSnackbar(text)
        }
    }

    LaunchedEffect(empty, howToAddSeen) {
        if (empty && !howToAddSeen) {
            showHowTo = true
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            ShelfAppBar(
                itemCount = itemCount,
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
            )
        },
        floatingActionButton = {
            if (!empty) {
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
                items(items, key = { it.id }) { item ->
                    ShelfItemCard(
                        item = item,
                        onShare = {
                            val file = viewModel.fileFor(item)
                            if (item.kind == ItemKind.TEXT ||
                                item.kind == ItemKind.LINK ||
                                file.exists()
                            ) {
                                ShareOut.send(context, item, file)
                            }
                        },
                        onRemove = { pendingRemove = item },
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
    menuExpanded: Boolean,
    onMenuChange: (Boolean) -> Unit,
    onAddText: () -> Unit,
    onHowToAdd: () -> Unit,
) {
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
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.shelf_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
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
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.how_to_add)) },
                        onClick = onHowToAdd,
                    )
                }
            }
        }
        if (itemCount > 0) {
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

@Composable
private fun ShelfItemCard(
    item: ShelfItem,
    onShare: () -> Unit,
    onRemove: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val file = remember(item.id) {
        java.io.File(context.filesDir, "shelf/${item.relativePath}")
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                if (dark) {
                    MaterialTheme.colorScheme.outline
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                CardShape,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
