package com.shobhankarthish.pocket.shelf.prefs

enum class Appearance {
    System,
    Light,
    Dark,
    ;

    fun isDark(systemDark: Boolean): Boolean = when (this) {
        System -> systemDark
        Light -> false
        Dark -> true
    }

    companion object {
        fun fromStore(raw: String?): Appearance =
            entries.find { it.name.equals(raw, ignoreCase = true) } ?: System
    }
}
