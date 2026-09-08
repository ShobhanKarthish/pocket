package com.shobhankarthish.pocket.shelf

sealed interface IngestResult {
    data class Ok(val item: ShelfItem) : IngestResult
    data object Unsupported : IngestResult
    data object Failed : IngestResult
}
