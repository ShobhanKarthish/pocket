package com.shobhankarthish.pocket.ui.shelf

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shobhankarthish.pocket.PocketApp
import com.shobhankarthish.pocket.shelf.BatchTally
import com.shobhankarthish.pocket.shelf.IngestResult
import com.shobhankarthish.pocket.shelf.ItemIngestor
import com.shobhankarthish.pocket.shelf.Selection
import com.shobhankarthish.pocket.shelf.ShelfItem
import com.shobhankarthish.pocket.shelf.ShelfMode
import com.shobhankarthish.pocket.shelf.ShelfOrder
import com.shobhankarthish.pocket.shelf.ShelfRepository
import com.shobhankarthish.pocket.shelf.prefs.HowToAddPrefs
import com.shobhankarthish.pocket.shelf.toInboundFile
import com.shobhankarthish.pocket.shelf.RemovalHold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ShelfViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as PocketApp).container
    private val repository: ShelfRepository = container.repository
    private val ingestor: ItemIngestor = container.ingestor
    private val howToAddPrefs: HowToAddPrefs = container.howToAddPrefs

    private val hold = MutableStateFlow<RemovalHold?>(null)
    private var nextRemovalToken = 1

    val items: StateFlow<List<ShelfItem>> = combine(
        repository.observeItems(),
        hold,
    ) { all, pending ->
        pending?.visible(all) ?: all
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList(),
    )

    val howToAddSeen: StateFlow<Boolean> = howToAddPrefs.seen.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        true,
    )

    private val _mode = MutableStateFlow<ShelfMode>(ShelfMode.Browse)
    val mode: StateFlow<ShelfMode> = _mode.asStateFlow()

    private val messages = Channel<UserMessage>(Channel.BUFFERED)
    val userMessages = messages.receiveAsFlow()

    init {
        viewModelScope.launch { repository.reconcile() }
    }

    fun ingest(uri: Uri) {
        viewModelScope.launch { ingestSuspending(uri) }
    }

    suspend fun ingestSuspending(uri: Uri) {
        messages.send(ingestOne(uri).toUserMessage())
    }

    suspend fun ingestMany(uris: List<Uri>) {
        if (uris.isEmpty()) return
        if (uris.size == 1) {
            ingestSuspending(uris.first())
            return
        }
        var tally = BatchTally()
        uris.forEach { uri ->
            tally = tally.plus(ingestOne(uri))
        }
        messages.send(tally.toUserMessage())
    }

    suspend fun ingestTextSuspending(text: String) {
        val message = try {
            ingestor.ingestText(text).toUserMessage()
        } catch (_: Exception) {
            UserMessage.Failed
        }
        messages.send(message)
    }

    fun ingestText(text: String) {
        viewModelScope.launch { ingestTextSuspending(text) }
    }

    private suspend fun ingestOne(uri: Uri): IngestResult = try {
        val inbound = withContext(Dispatchers.IO) {
            getApplication<Application>().contentResolver.toInboundFile(uri)
        }
        ingestor.ingest(inbound)
    } catch (_: Exception) {
        IngestResult.Failed
    }

    fun remove(item: ShelfItem) {
        requestRemove(listOf(item))
    }

    fun removeSelected() {
        val selecting = _mode.value as? ShelfMode.Selecting ?: return
        requestRemove(Selection.inShelfOrder(items.value, selecting.ids))
    }

    fun undoRemoval(token: Int) {
        if (hold.value?.matches(token) == true) hold.value = null
    }

    fun commitRemoval(token: Int) {
        val current = hold.value ?: return
        if (!current.matches(token)) return
        viewModelScope.launch {
            repository.remove(current.items)
            if (hold.value?.matches(token) == true) hold.value = null
        }
    }

    private fun requestRemove(targets: List<ShelfItem>) {
        if (targets.isEmpty()) return
        val token = nextRemovalToken++
        hold.value = RemovalHold(token, targets)
        if (_mode.value is ShelfMode.Selecting) _mode.value = ShelfMode.Browse
        viewModelScope.launch {
            messages.send(
                if (targets.size == 1) {
                    UserMessage.Removed(token)
                } else {
                    UserMessage.RemovedSome(token, targets.size)
                },
            )
        }
    }

    fun selectedInShelfOrder(): List<ShelfItem> {
        val selecting = _mode.value as? ShelfMode.Selecting ?: return emptyList()
        return Selection.inShelfOrder(items.value, selecting.ids)
    }

    fun enterSelecting(id: String? = null) {
        _mode.value = ShelfMode.Selecting(id?.let { setOf(it) } ?: Selection.none())
    }

    fun toggleSelected(id: String) {
        val current = _mode.value as? ShelfMode.Selecting ?: return
        _mode.value = current.copy(ids = Selection.toggle(current.ids, id))
    }

    fun selectAll() {
        _mode.value = ShelfMode.Selecting(Selection.all(items.value.map { it.id }))
    }

    fun deselectAll() {
        _mode.value = ShelfMode.Selecting(Selection.none())
    }

    fun enterArranging() {
        _mode.value = ShelfMode.Arranging
    }

    fun exitMode() {
        _mode.value = ShelfMode.Browse
    }

    fun moveItem(id: String, delta: Int) {
        viewModelScope.launch {
            repository.reorder(ShelfOrder.move(items.value.map { it.id }, id, delta))
        }
    }

    fun moveItemTo(from: Int, to: Int) {
        viewModelScope.launch {
            repository.reorder(ShelfOrder.moveTo(items.value.map { it.id }, from, to))
        }
    }

    fun fileFor(item: ShelfItem): File = repository.fileFor(item)

    fun markHowToAddSeen() {
        viewModelScope.launch { howToAddPrefs.markSeen() }
    }

    fun note(message: UserMessage) {
        viewModelScope.launch { messages.send(message) }
    }
}

sealed interface UserMessage {
    data object Added : UserMessage
    data object Unsupported : UserMessage
    data object Failed : UserMessage
    data class Removed(val token: Int) : UserMessage
    data class AddedSome(val added: Int, val attempted: Int) : UserMessage
    data class RemovedSome(val token: Int, val count: Int) : UserMessage
    data class SharePartial(val shared: Int, val skipped: Int) : UserMessage
    data object ShareNone : UserMessage
    data object ShareMixed : UserMessage
    data object Copied : UserMessage
    data class OpenFailed(val text: String) : UserMessage
}

private fun IngestResult.toUserMessage(): UserMessage = when (this) {
    is IngestResult.Ok -> UserMessage.Added
    IngestResult.Unsupported -> UserMessage.Unsupported
    IngestResult.Failed -> UserMessage.Failed
}

private fun BatchTally.toUserMessage(): UserMessage = when {
    attempted <= 1 -> when {
        added == 1 -> UserMessage.Added
        unsupported == 1 -> UserMessage.Unsupported
        else -> UserMessage.Failed
    }
    added == attempted -> UserMessage.AddedSome(added, attempted)
    added == 0 && unsupported == attempted -> UserMessage.Unsupported
    added == 0 -> UserMessage.Failed
    else -> UserMessage.AddedSome(added, attempted)
}
