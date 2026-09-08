package com.shobhankarthish.pocket.shelf

data class BatchTally(
    val added: Int = 0,
    val unsupported: Int = 0,
    val failed: Int = 0,
) {
    val attempted: Int get() = added + unsupported + failed

    fun plus(result: IngestResult): BatchTally = when (result) {
        is IngestResult.Ok -> copy(added = added + 1)
        IngestResult.Unsupported -> copy(unsupported = unsupported + 1)
        IngestResult.Failed -> copy(failed = failed + 1)
    }
}
