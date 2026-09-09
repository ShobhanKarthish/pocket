package com.shobhankarthish.pocket.shelf

data class RemovalHold(
    val token: Int,
    val items: List<ShelfItem>,
) {
    fun visible(from: List<ShelfItem>): List<ShelfItem> {
        val ids = items.map { it.id }.toSet()
        return from.filter { it.id !in ids }
    }

    fun matches(token: Int): Boolean = this.token == token
}
