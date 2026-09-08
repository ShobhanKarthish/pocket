package com.shobhankarthish.pocket.shelf

sealed interface ShelfMode {
    data object Browse : ShelfMode
    data class Selecting(val ids: Set<String>) : ShelfMode
    data object Arranging : ShelfMode
}
