package com.befitnessapp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AppLanguage {
    ES,
    EN
}

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

enum class WeightUnit {
    KG,
    LB
}

data class AppSettingsState(
    val language: AppLanguage = AppLanguage.ES,
    val theme: AppTheme = AppTheme.SYSTEM,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val weeklyGoal: Int = 3,
    val remindersEnabled: Boolean = false
)

object AppSettings {

    private val _state = MutableStateFlow(AppSettingsState())
    val state: StateFlow<AppSettingsState> = _state

    fun setLanguage(language: AppLanguage) {
        _state.value = _state.value.copy(language = language)
    }

    fun setTheme(theme: AppTheme) {
        _state.value = _state.value.copy(theme = theme)
    }

    fun setWeightUnit(unit: WeightUnit) {
        _state.value = _state.value.copy(weightUnit = unit)
    }

    fun setWeeklyGoal(goal: Int) {
        val normalized = goal.coerceIn(1, 7)
        _state.value = _state.value.copy(weeklyGoal = normalized)
    }

    fun setRemindersEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(remindersEnabled = enabled)
    }
}
