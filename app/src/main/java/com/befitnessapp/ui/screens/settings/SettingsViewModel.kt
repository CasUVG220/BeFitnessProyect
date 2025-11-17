package com.befitnessapp.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.befitnessapp.AppLanguage
import com.befitnessapp.AppSettings
import com.befitnessapp.AppSettingsState
import com.befitnessapp.AppTheme
import com.befitnessapp.WeightUnit
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel : ViewModel() {

    val state: StateFlow<AppSettingsState> = AppSettings.state

    fun setLanguage(language: AppLanguage) {
        AppSettings.setLanguage(language)
    }

    fun setTheme(theme: AppTheme) {
        AppSettings.setTheme(theme)
    }

    fun setWeightUnit(unit: WeightUnit) {
        AppSettings.setWeightUnit(unit)
    }

    fun setWeeklyGoal(goal: Int) {
        AppSettings.setWeeklyGoal(goal)
    }

    fun setRemindersEnabled(enabled: Boolean) {
        AppSettings.setRemindersEnabled(enabled)
    }
}
