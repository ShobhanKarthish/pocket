package com.shobhankarthish.pocket.shelf.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HowToAddPrefs(private val store: DataStore<Preferences>) {
    val seen: Flow<Boolean> = store.data.map { prefs -> prefs[SEEN] == true }

    suspend fun markSeen() {
        store.edit { prefs -> prefs[SEEN] = true }
    }

    private companion object {
        val SEEN = booleanPreferencesKey("how_to_add_seen")
    }
}
