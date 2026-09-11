package com.shobhankarthish.pocket.shelf.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.shobhankarthish.pocket.bubble.BubbleChrome
import com.shobhankarthish.pocket.bubble.BubblePlacement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsPrefs(private val store: DataStore<Preferences>) {
    val appearance: Flow<Appearance> = store.data.map { prefs ->
        Appearance.fromStore(prefs[APPEARANCE])
    }

    val haptics: Flow<Boolean> = store.data.map { prefs ->
        prefs[HAPTICS] ?: true
    }

    val bubbleEnabled: Flow<Boolean> = store.data.map { prefs ->
        prefs[BUBBLE_ENABLED] ?: true
    }

    val overlayPromptDismissed: Flow<Boolean> = store.data.map { prefs ->
        prefs[OVERLAY_PROMPT_DISMISSED] == true
    }

    val bubblePlacement: Flow<BubblePlacement> = store.data.map { prefs ->
        BubblePlacement(
            onLeft = prefs[BUBBLE_ON_LEFT] == true,
            yFraction = prefs[BUBBLE_Y_FRACTION] ?: BubbleChrome.DefaultYFraction,
        )
    }

    suspend fun setAppearance(value: Appearance) {
        store.edit { prefs -> prefs[APPEARANCE] = value.name }
    }

    suspend fun setHaptics(enabled: Boolean) {
        store.edit { prefs -> prefs[HAPTICS] = enabled }
    }

    suspend fun setBubbleEnabled(enabled: Boolean) {
        store.edit { prefs -> prefs[BUBBLE_ENABLED] = enabled }
    }

    suspend fun setOverlayPromptDismissed() {
        store.edit { prefs -> prefs[OVERLAY_PROMPT_DISMISSED] = true }
    }

    suspend fun setBubblePlacement(placement: BubblePlacement) {
        store.edit { prefs ->
            prefs[BUBBLE_ON_LEFT] = placement.onLeft
            prefs[BUBBLE_Y_FRACTION] = placement.yFraction.coerceIn(0f, 1f)
        }
    }

    private companion object {
        val APPEARANCE = stringPreferencesKey("appearance")
        val HAPTICS = booleanPreferencesKey("haptics")
        val BUBBLE_ENABLED = booleanPreferencesKey("bubble_enabled")
        val OVERLAY_PROMPT_DISMISSED = booleanPreferencesKey("overlay_prompt_dismissed")
        val BUBBLE_ON_LEFT = booleanPreferencesKey("bubble_on_left")
        val BUBBLE_Y_FRACTION = floatPreferencesKey("bubble_y_fraction")
    }
}
