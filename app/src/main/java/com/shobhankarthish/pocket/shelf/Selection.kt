package com.shobhankarthish.pocket.shelf

object Selection {
    fun toggle(ids: Set<String>, id: String): Set<String> =
        if (id in ids) ids - id else ids + id

    fun all(ids: List<String>): Set<String> = ids.toSet()

    fun none(): Set<String> = emptySet()

    fun <T> inShelfOrder(items: List<T>, idOf: (T) -> String, selected: Set<String>): List<T> =
        items.filter { idOf(it) in selected }

    fun inShelfOrder(items: List<ShelfItem>, selected: Set<String>): List<ShelfItem> =
        inShelfOrder(items, ShelfItem::id, selected)
}
