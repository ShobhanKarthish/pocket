package com.shobhankarthish.pocket.shelf

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.util.UUID

class ItemIngestor(
    private val files: ShelfFileStore,
    private val repository: ShelfRepository,
    private val io: CoroutineDispatcher = Dispatchers.IO,
    private val ids: () -> String = { UUID.randomUUID().toString() },
    private val nowMs: () -> Long = { System.currentTimeMillis() },
) {
    suspend fun ingest(inbound: InboundFile): IngestResult = withContext(io) {
        val kind = ItemKind.fromMime(inbound.mimeType) ?: return@withContext IngestResult.Unsupported
        val mime = inbound.mimeType!!.substringBefore(';').trim()
        val displayName = DisplayNames.sanitize(inbound.displayName, kind)
        val id = ids()
        val relativePath = "$id.${DisplayNames.extensionOf(displayName, kind)}"
        val draft = ShelfItem(
            id = id,
            displayName = displayName,
            mimeType = mime,
            byteSize = 0,
            relativePath = relativePath,
            createdAtEpochMs = nowMs(),
        )
        try {
            val stream = inbound.openStream() ?: return@withContext IngestResult.Failed
            stream.use { input ->
                IngestResult.Ok(repository.add(relativePath, input, draft))
            }
        } catch (_: Exception) {
            files.delete(relativePath)
            IngestResult.Failed
        }
    }

    suspend fun ingestText(raw: String): IngestResult = withContext(io) {
        val payload = TextInbound.parse(raw) ?: return@withContext IngestResult.Failed
        val id = ids()
        val relativePath = "$id.${payload.extension}"
        val draft = ShelfItem(
            id = id,
            displayName = payload.displayName,
            mimeType = payload.mimeType,
            byteSize = 0,
            relativePath = relativePath,
            createdAtEpochMs = nowMs(),
        )
        try {
            ByteArrayInputStream(payload.body.toByteArray(Charsets.UTF_8)).use { input ->
                IngestResult.Ok(repository.add(relativePath, input, draft))
            }
        } catch (_: Exception) {
            files.delete(relativePath)
            IngestResult.Failed
        }
    }
}
