package com.befitnessapp.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.befitnessapp.prefs.AppLanguage
import com.befitnessapp.prefs.AppSettings
import com.befitnessapp.prefs.AppTheme
import com.befitnessapp.prefs.SettingsState
import com.befitnessapp.prefs.WeightUnit
import com.befitnessapp.ui.localization.LocalStrings
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val settingsFlow = remember { AppSettings.observe(context) }
    val settingsState by settingsFlow.collectAsState(initial = SettingsState())

    val strings = LocalStrings.current.settings

    val initialGoal = if (settingsState.weeklyGoal > 0f) {
        settingsState.weeklyGoal.toInt().toString()
    } else {
        ""
    }
    var goalText by remember(settingsState.weeklyGoal) { mutableStateOf(initialGoal) }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.backButtonContentDescription
                        )
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = strings.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Idioma
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(strings.languageSectionTitle, style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isEs = settingsState.language == AppLanguage.ES
                        AssistChip(
                            onClick = {
                                scope.launch {
                                    AppSettings.setLanguage(context, AppLanguage.ES)
                                }
                            },
                            label = { Text(strings.languageEsLabel) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isEs)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        )

                        val isEn = settingsState.language == AppLanguage.EN
                        AssistChip(
                            onClick = {
                                scope.launch {
                                    AppSettings.setLanguage(context, AppLanguage.EN)
                                }
                            },
                            label = { Text(strings.languageEnLabel) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isEn)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }

            // Tema
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(strings.themeSectionTitle, style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeChip(
                            label = strings.themeSystemLabel,
                            selected = settingsState.theme == AppTheme.SYSTEM
                        ) {
                            scope.launch { AppSettings.setTheme(context, AppTheme.SYSTEM) }
                        }
                        ThemeChip(
                            label = strings.themeLightLabel,
                            selected = settingsState.theme == AppTheme.LIGHT
                        ) {
                            scope.launch { AppSettings.setTheme(context, AppTheme.LIGHT) }
                        }
                        ThemeChip(
                            label = strings.themeDarkLabel,
                            selected = settingsState.theme == AppTheme.DARK
                        ) {
                            scope.launch { AppSettings.setTheme(context, AppTheme.DARK) }
                        }
                    }
                }
            }

            // Unidades
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(strings.unitsSectionTitle, style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isKg = settingsState.weightUnit == WeightUnit.KG
                        AssistChip(
                            onClick = {
                                scope.launch {
                                    AppSettings.setWeightUnit(context, WeightUnit.KG)
                                }
                            },
                            label = { Text(strings.unitsKgLabel) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isKg)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        )

                        val isLb = settingsState.weightUnit == WeightUnit.LB
                        AssistChip(
                            onClick = {
                                scope.launch {
                                    AppSettings.setWeightUnit(context, WeightUnit.LB)
                                }
                            },
                            label = { Text(strings.unitsLbLabel) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isLb)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }

            // Meta semanal
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        strings.weeklyGoalSectionTitle,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "En peso total levantado (${if (settingsState.weightUnit == WeightUnit.KG) "kg·rep" else "lb·rep"})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = goalText,
                        onValueChange = { goalText = it.filter(Char::isDigit).take(5) },
                        label = { Text(strings.weeklyGoalHint) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    val parsedGoal = goalText.toFloatOrNull()
                    Button(
                        enabled = parsedGoal != null && parsedGoal > 0f,
                        onClick = {
                            parsedGoal?.let { value ->
                                scope.launch {
                                    AppSettings.setWeeklyGoal(context, value)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.weeklyGoalSaveButton)
                    }
                }
            }

            // Recordatorios
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(strings.remindersSectionTitle, style = MaterialTheme.typography.titleMedium)
                        Text(
                            strings.remindersDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settingsState.remindersEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                AppSettings.setRemindersEnabled(context, enabled)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            else
                MaterialTheme.colorScheme.surface
        )
    )
}
