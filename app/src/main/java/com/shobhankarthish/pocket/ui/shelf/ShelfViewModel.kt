package com.shobhankarthish.pocket.ui.shelf

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shobhankarthish.pocket.PocketApp
import com.shobhankarthish.pocket.shelf.IngestResult
import com.shobhankarthish.pocket.shelf.ItemIngestor
import com.shobhankarthish.pocket.shelf.ShelfItem
import com.shobhankarthish.pocket.shelf.ShelfRepository
import com.shobhankarthish.pocket.shelf.prefs.HowToAddPrefs
import com.shobhankarthish.pocket.shelf.toInboundFile
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class ShelfViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as PocketApp).container
    private val repository: ShelfRepository = container.repository
    private val ingestor: ItemIngestor = container.ingestor
    private val howToAddPrefs: HowToAddPrefs = container.howToAddPrefs

    val items: StateFlow<List<ShelfItem>> = repository.observeItems().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    val howToAddSeen: StateFlow<Boolean> = howToAddPrefs.seen.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        true,
    )

    private val messages = Channel<UserMessage>(Channel.BUFFERED)
    val userMessages = messages.receiveAsFlow()

    init {
        viewModelScope.launch { repository.reconcile() }
    }

    fun ingest(uri: Uri) {
        viewModelScope.launch {
            val inbound = getApplication<Application>().contentResolver.toInboundFile(uri)
            val result = when (ingestor.ingest(inbound)) {
                is IngestResult.Ok -> UserMessage.Added
                IngestResult.Unsupported -> UserMessage.Unsupported
                IngestResult.Failed -> UserMessage.Failed
            }
            messages.send(result)
        }
    }

    fun remove(item: ShelfItem) {
        viewModelScope.launch {
            repository.remove(item)
            messages.send(UserMessage.Removed)
        }
    }

    fun fileFor(item: ShelfItem): File = repository.fileFor(item)

    fun markHowToAddSeen() {
        viewModelScope.launch { howToAddPrefs.markSeen() }
    }
}

enum class UserMessage {
    Added,
    Unsupported,
    Failed,
    Removed,
}
