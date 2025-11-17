package com.befitnessapp.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_settings")

enum class AppLanguage { ES, EN }
enum class WeightUnit { KG, LB }
enum class AppTheme { SYSTEM, LIGHT, DARK }

data class SettingsState(
    val language: AppLanguage = AppLanguage.ES,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val theme: AppTheme = AppTheme.SYSTEM,
    val weeklyGoal: Float = 0f,
    val remindersEnabled: Boolean = false
)

object AppSettings {

    private val KEY_LANGUAGE = stringPreferencesKey("language")
    private val KEY_WEIGHT_UNIT = stringPreferencesKey("weight_unit")
    private val KEY_THEME = stringPreferencesKey("theme")
    private val KEY_WEEKLY_GOAL = floatPreferencesKey("weekly_goal")
    private val KEY_REMINDERS = booleanPreferencesKey("reminders_enabled")

    fun observe(context: Context): Flow<SettingsState> =
        context.dataStore.data.map { prefs ->
            SettingsState(
                language = when (prefs[KEY_LANGUAGE]) {
                    "en" -> AppLanguage.EN
                    else -> AppLanguage.ES
                },
                weightUnit = when (prefs[KEY_WEIGHT_UNIT]) {
                    "lb" -> WeightUnit.LB
                    else -> WeightUnit.KG
                },
                theme = when (prefs[KEY_THEME]) {
                    "light" -> AppTheme.LIGHT
                    "dark" -> AppTheme.DARK
                    else -> AppTheme.SYSTEM
                },
                weeklyGoal = prefs[KEY_WEEKLY_GOAL] ?: 0f,
                remindersEnabled = prefs[KEY_REMINDERS] ?: false
            )
        }

    suspend fun setLanguage(context: Context, lang: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = when (lang) {
                AppLanguage.ES -> "es"
                AppLanguage.EN -> "en"
            }
        }
    }

    suspend fun setWeightUnit(context: Context, unit: WeightUnit) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WEIGHT_UNIT] = when (unit) {
                WeightUnit.KG -> "kg"
                WeightUnit.LB -> "lb"
            }
        }
    }

    suspend fun setTheme(context: Context, theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = when (theme) {
                AppTheme.LIGHT -> "light"
                AppTheme.DARK -> "dark"
                AppTheme.SYSTEM -> "system"
            }
        }
    }

    suspend fun setWeeklyGoal(context: Context, goal: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WEEKLY_GOAL] = goal
        }
    }

    suspend fun setRemindersEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_REMINDERS] = enabled
        }
    }
}
