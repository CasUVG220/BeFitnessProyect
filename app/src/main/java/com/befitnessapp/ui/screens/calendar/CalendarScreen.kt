package com.befitnessapp.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.befitnessapp.Graph
import com.befitnessapp.data.local.dao.WorkoutWithSets
import com.befitnessapp.data.local.entity.WorkoutSetEntity
import com.befitnessapp.domain.catalog.Catalogo
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    onAddWorkoutForDate: (LocalDate) -> Unit
) {
    val repo = Graph.workoutRepository

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val from = remember(currentMonth) { currentMonth.atDay(1) }
    val to = remember(currentMonth) { currentMonth.atEndOfMonth() }

    val byDate by remember(from, to) {
        repo.observeRange(from, to).map { list -> list.groupBy { it.workout.date } }
    }.collectAsState(initial = emptyMap())

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var openForDate by remember { mutableStateOf<LocalDate?>(null) }

    val monthTitle = remember(currentMonth) {
        val monthName = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        "${monthName.replaceFirstChar { it.titlecase() }} ${currentMonth.year}"
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Surface(tonalElevation = 1.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onBack) { Text("Atrás") }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mes anterior")
                    }
                    Text(
                        monthTitle,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Mes siguiente")
                    }
                }
            }
            Divider()

            MonthGrid(
                month = currentMonth,
                byDate = byDate,
                selectedDate = openForDate,
                onSelect = { d -> openForDate = d }
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = MaterialTheme.colorScheme.primaryContainer, text = "Con entreno")
                LegendItem(color = MaterialTheme.colorScheme.primary, text = "Seleccionado")
            }
        }

        openForDate?.let { theDate ->
            val items = byDate[theDate].orEmpty()
            ModalBottomSheet(
                onDismissRequest = { openForDate = null },
                sheetState = sheetState
            ) {
                DayDetail(
                    date = theDate,
                    items = items,
                    onClose = { openForDate = null },
                    onAddLog = onAddWorkoutForDate
                )
            }
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    byDate: Map<LocalDate, List<WorkoutWithSets>>,
    selectedDate: LocalDate?,
    onSelect: (LocalDate) -> Unit
) {
    val firstOfMonth = month.atDay(1)
    val length = month.lengthOfMonth()
    val firstDayOfWeekIndex = (firstOfMonth.dayOfWeek.value % 7) // 0=Domingo
    val today = LocalDate.now()

    val weeks = remember(month) {
        buildList {
            var dayNum = 1
            while (dayNum <= length) {
                val row = MutableList<LocalDate?>(7) { null }
                for (col in 0 until 7) {
                    if (isEmpty() && col < firstDayOfWeekIndex) continue
                    if (dayNum <= length) {
                        row[col] = month.atDay(dayNum)
                        dayNum++
                    }
                }
                add(row)
            }
        }
    }

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb").forEach { label ->
                Text(label, modifier = Modifier.width(40.dp), style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(Modifier.height(8.dp))

        weeks.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                row.forEach { d ->
                    DayCell(
                        date = d,
                        isToday = d == today,
                        isSelected = d == selectedDate,
                        hasWorkout = d != null && byDate[d].orEmpty().isNotEmpty(),
                        onClick = { if (d != null) onSelect(d) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    isToday: Boolean,
    isSelected: Boolean,
    hasWorkout: Boolean,
    onClick: () -> Unit
) {
    val text = date?.dayOfMonth?.toString() ?: ""

    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        hasWorkout -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        hasWorkout -> MaterialTheme.colorScheme.onPrimaryContainer
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val fontWeight = if (isToday && !isSelected) FontWeight.Bold else FontWeight.Normal

    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = if (hasWorkout && !isSelected) 2.dp else 0.dp,
        color = Color.Transparent, // Surface is for elevation and shape, background is in the Box
        modifier = Modifier
            .size(40.dp)
            .clickable(enabled = date != null, onClick = onClick)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = textColor, fontWeight = fontWeight)
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Dot(color = color)
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun Dot(color: Color, size: Dp = 14.dp) {
    Box(
        Modifier
            .size(size)
            .background(color, shape = MaterialTheme.shapes.small)
    )
}

@Composable
private fun DayDetail(
    date: LocalDate,
    items: List<WorkoutWithSets>,
    onClose: () -> Unit,
    onAddLog: (LocalDate) -> Unit
) {
    val fmt = remember { DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy") }
    Column(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${fmt.format(date)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "Cerrar") }
        }

        if (items.isEmpty()) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("No hay entrenamiento registrado en este día.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                OutlinedButton(
                    onClick = { onAddLog(date) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar entrenamiento")
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    val totals = remember(items) { computeTotals(items) }
                    ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Resumen", style = MaterialTheme.typography.titleMedium)
                            Text("Volumen: ${prettyFloat(totals.volume)}", style = MaterialTheme.typography.bodySmall)
                            Text("Reps: ${totals.reps}  ·  Sets: ${totals.sets}  ·  Ejercicios: ${totals.exercises}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                items(items) { ww ->
                    ExerciseCard(ww)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ExerciseCard(ww: WorkoutWithSets) {
    val byEx = remember(ww) { ww.sets.groupBy { it.exerciseId } }
    Card(modifier = Modifier.padding(horizontal = 16.dp)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(ww.workout.notes ?: "Entrenamiento", style = MaterialTheme.typography.titleMedium)
            Divider(modifier = Modifier.padding(vertical = 4.dp))

            byEx.forEach { (exId, sets) ->
                val totals = remember(sets) { computeSetsTotals(sets) }
                Text("• ${exerciseName(exId)}", fontWeight = FontWeight.SemiBold)
                Text("  Volumen: ${prettyFloat(totals.volume)}  ·  Reps: ${totals.reps}  ·  Sets: ${totals.sets}")
                sets.forEachIndexed { idx, s ->
                    Text("    Set ${idx + 1}: ${s.reps} reps  @ ${prettyFloat(s.weight)}")
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

/* ====== Helpers de totales ====== */

private data class Totals(val volume: Float, val reps: Int, val sets: Int, val exercises: Int)

private fun computeTotals(list: List<WorkoutWithSets>): Totals {
    var vol = 0f
    var reps = 0
    var sets = 0
    val exercises = mutableSetOf<Int>()
    list.forEach { ww ->
        ww.sets.forEach { s ->
            vol += (s.reps * s.weight)
            reps += s.reps
            sets += 1
            exercises.add(s.exerciseId)
        }
    }
    return Totals(vol, reps, sets, exercises.size)
}

private fun computeSetsTotals(sets: List<WorkoutSetEntity>): Totals {
    var vol = 0f
    var reps = 0
    var cnt = 0
    sets.forEach { s ->
        vol += (s.reps * s.weight)
        reps += s.reps
        cnt += 1
    }
    return Totals(vol, reps, cnt, 1)
}

private fun prettyFloat(f: Float): String {
    val s = String.format("%.1f", f)
    return if (s.endsWith(".0")) s.dropLast(2) else s
}

private fun exerciseName(id: Int): String =
    Catalogo.allExercises.firstOrNull { it.id == id }?.name ?: "Ejercicio $id"
