package com.befitnessapp.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    onAddWorkoutForDate: (LocalDate) -> Unit  // <- NUEVO: se pasa la fecha elegida
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
        currentMonth.month.name.lowercase().replaceFirstChar { it.titlecase() } + " " + currentMonth.year
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {

            /* ---------- Top bar simple ---------- */
            Surface(tonalElevation = 1.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onBack) { Text("Atrás") }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) { Text("<") }
                    Text(
                        monthTitle,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    TextButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) { Text(">") }
                }
            }
            Divider()

            MonthGrid(
                month = currentMonth,
                byDate = byDate,
                onSelect = { d -> openForDate = d }
            )

            // Leyenda simple
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Dot(color = MaterialTheme.colorScheme.primary)
                Text("Con entrenamiento", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(12.dp))
                Dot(color = MaterialTheme.colorScheme.outline)
                Text("Sin datos", style = MaterialTheme.typography.bodySmall)
            }
        }

        // Detalle del día
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
                    onAddLog = { d -> onAddWorkoutForDate(d) } // <- pasa la fecha al host
                )
            }
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    byDate: Map<LocalDate, List<WorkoutWithSets>>,
    onSelect: (LocalDate) -> Unit
) {
    val firstOfMonth = month.atDay(1)
    val length = month.lengthOfMonth()
    val firstDayOfWeekIndex = (firstOfMonth.dayOfWeek.value % 7) // 0=Domingo

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
private fun DayCell(date: LocalDate?, hasWorkout: Boolean, onClick: () -> Unit) {
    val text = date?.dayOfMonth?.toString() ?: ""
    val bg = if (hasWorkout) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else Color.Transparent
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = if (hasWorkout) 2.dp else 0.dp,
        modifier = Modifier
            .size(40.dp)
            .background(Color.Transparent)
            .clickable(enabled = date != null, onClick = onClick)
    ) {
        Box(Modifier.fillMaxSize().background(bg), contentAlignment = Alignment.Center) {
            Text(text)
        }
    }
}

@Composable
private fun Dot(color: Color, size: Dp = 10.dp) {
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
    onAddLog: (LocalDate) -> Unit   // <- recibe fecha
) {
    val fmt = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Detalle • ${fmt.format(date)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = onClose) { Text("Cerrar") }
        }

        if (items.isEmpty()) {
            Text("No hay entrenamiento registrado en este día.", style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(
                onClick = { onAddLog(date) },   // <- abre AddLog con la fecha del día
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Loggear entrenamiento")
            }
        } else {
            val totals = remember(items) { computeTotals(items) }
            ElevatedCard {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Resumen", style = MaterialTheme.typography.titleMedium)
                    Text("Volumen: ${prettyFloat(totals.volume)}", style = MaterialTheme.typography.bodySmall)
                    Text("Reps: ${totals.reps}  ·  Sets: ${totals.sets}  ·  Ejercicios: ${totals.exercises}", style = MaterialTheme.typography.bodySmall)
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items) { ww ->
                    ExerciseCard(ww)
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(ww: WorkoutWithSets) {
    val byEx = remember(ww) { ww.sets.groupBy { it.exerciseId } }
    Card {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Entrenamiento", style = MaterialTheme.typography.titleMedium)
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
