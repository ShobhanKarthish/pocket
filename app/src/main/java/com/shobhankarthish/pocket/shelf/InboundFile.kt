package com.shobhankarthish.pocket.shelf

import java.io.InputStream

data class InboundFile(
    val mimeType: String?,
    val displayName: String?,
    val openStream: () -> InputStream?,
)
