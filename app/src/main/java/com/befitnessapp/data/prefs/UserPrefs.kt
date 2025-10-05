package com.befitnessapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPrefsKeys {
    val unitIsKg: Preferences.Key<Boolean> = booleanPreferencesKey("unit_is_kg") // true=kg, false=lb
    val hasSeenOnboarding: Preferences.Key<Boolean> = booleanPreferencesKey("has_seen_onboarding")
    val daysPerWeek: Preferences.Key<Int> = intPreferencesKey("days_per_week")
}

class UserPrefs(private val context: Context) {
    val unitIsKgFlow: Flow<Boolean> = context.dataStore.data.map { it[UserPrefsKeys.unitIsKg] ?: true }

    suspend fun setUnitIsKg(isKg: Boolean) {
        context.dataStore.edit { it[UserPrefsKeys.unitIsKg] = isKg }
    }

    val hasSeenOnboardingFlow: Flow<Boolean> = context.dataStore.data.map { it[UserPrefsKeys.hasSeenOnboarding] ?: false }
    suspend fun setHasSeenOnboarding(seen: Boolean) {
        context.dataStore.edit { it[UserPrefsKeys.hasSeenOnboarding] = seen }
    }
}
