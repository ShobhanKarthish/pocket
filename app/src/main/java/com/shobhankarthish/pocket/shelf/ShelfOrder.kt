package com.shobhankarthish.pocket.shelf

object ShelfOrder {
    fun move(ids: List<String>, id: String, delta: Int): List<String> {
        val from = ids.indexOf(id)
        if (from < 0) return ids
        return moveTo(ids, from, from + delta)
    }

    fun moveTo(ids: List<String>, from: Int, to: Int): List<String> {
        if (from !in ids.indices || to !in ids.indices || from == to) return ids
        val next = ids.toMutableList()
        val id = next.removeAt(from)
        next.add(to, id)
        return next
    }

    fun sortIndexes(ids: List<String>): Map<String, Long> {
        val n = ids.size
        return ids.mapIndexed { index, id -> id to (n - index).toLong() }.toMap()
    }
}
