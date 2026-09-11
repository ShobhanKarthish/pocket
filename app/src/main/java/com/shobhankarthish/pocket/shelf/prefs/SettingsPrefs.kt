package com.shobhankarthish.pocket.shelf.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsPrefs(private val store: DataStore<Preferences>) {
    val appearance: Flow<Appearance> = store.data.map { prefs ->
        Appearance.fromStore(prefs[APPEARANCE])
    }

    val haptics: Flow<Boolean> = store.data.map { prefs ->
        prefs[HAPTICS] ?: true
    }

    suspend fun setAppearance(value: Appearance) {
        store.edit { prefs -> prefs[APPEARANCE] = value.name }
    }

    suspend fun setHaptics(enabled: Boolean) {
        store.edit { prefs -> prefs[HAPTICS] = enabled }
    }

    private companion object {
        val APPEARANCE = stringPreferencesKey("appearance")
        val HAPTICS = booleanPreferencesKey("haptics")
    }
}
