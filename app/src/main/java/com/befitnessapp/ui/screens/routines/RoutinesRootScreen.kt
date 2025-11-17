package com.befitnessapp.ui.screens.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.befitnessapp.domain.routines.RoutineDetail
import com.befitnessapp.routines.RoutinesServiceLocator
import com.befitnessapp.ui.localization.LocalStrings
import com.befitnessapp.ui.screens.routines.nav.RoutinesRoute
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun RoutinesScreen(onBack: () -> Unit) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = RoutinesRoute.Home
    ) {
        composable<RoutinesRoute.Home> {
            RoutinesHome(
                onBack = onBack,
                goList = { nav.navigate(RoutinesRoute.List) },
                goBuilder = { nav.navigate(RoutinesRoute.Builder()) }
            )
        }

        composable<RoutinesRoute.List> {
            RoutinesList(
                onBack = { nav.popBackStack() },
                goBuilder = { nav.navigate(RoutinesRoute.Builder()) },
                onOpen = { detail ->
                    nav.navigate(RoutinesRoute.Builder(detail.routine.id))
                }
            )
        }

        composable<RoutinesRoute.Builder> { backStackEntry ->
            val args = backStackEntry.toRoute<RoutinesRoute.Builder>()
            val ctx = LocalContext.current
            val repo = remember(ctx) { RoutinesServiceLocator.repository(ctx) }
            var initial by remember { mutableStateOf<RoutineDetail?>(null) }

            LaunchedEffect(args.routineId) {
                initial = args.routineId?.let { repo.getRoutine(it) }
            }

            RoutineBuilderScreen(
                onBack = { nav.popBackStack() },
                initial = initial
            )
        }
    }
}

@Composable
private fun RoutinesHome(
    onBack: () -> Unit,
    goList: () -> Unit,
    goBuilder: () -> Unit
) {
    val strings = LocalStrings.current

    Scaffold { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    strings.routines.homeTitle,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = onBack) {
                    Text(strings.routines.homeBackButton)
                }
            }

            Text(
                strings.routines.homeQuestion,
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = goList,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.routines.homeMyRoutinesButton)
            }

            OutlinedButton(
                onClick = goBuilder,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.routines.homeCreateNewButton)
            }
        }
    }
}

@Composable
private fun RoutinesList(
    onBack: () -> Unit,
    goBuilder: () -> Unit,
    onOpen: (RoutineDetail) -> Unit
) {
    val strings = LocalStrings.current
    val ctx = LocalContext.current
    val repo = remember(ctx) { RoutinesServiceLocator.repository(ctx) }
    val scope = rememberCoroutineScope()

    var routines by remember { mutableStateOf<List<RoutineDetail>>(emptyList()) }
    LaunchedEffect(repo) {
        repo.observeRoutines().collectLatest { routines = it }
    }

    var confirmDelete by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }
    var pendingDeleteName by remember { mutableStateOf("") }

    Scaffold { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    strings.routines.listTitle,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = onBack) {
                    Text(strings.routines.listBackButton)
                }
            }

            if (routines.isEmpty()) {
                Text(strings.routines.listEmptyText)
                OutlinedButton(onClick = goBuilder) {
                    Text(strings.routines.listEmptyCreateButton)
                }
                return@Column
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(routines) { _, r ->
                    val totalExercises = r.exercises.size
                    val totalSets = r.exercises.sumOf { it.sets.size }

                    Card {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                r.routine.name.ifBlank { strings.routines.listCardFallbackName },
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "${strings.routines.listCardExercisesLabel}: $totalExercises · " +
                                        "${strings.routines.listCardTotalSetsLabel}: $totalSets",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(onClick = { onOpen(r) }) {
                                    Text(strings.routines.listOpenButton)
                                }
                                Spacer(Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        pendingDeleteId = r.routine.id
                                        pendingDeleteName =
                                            r.routine.name.ifBlank { strings.routines.listCardFallbackName }
                                        confirmDelete = true
                                    }
                                ) {
                                    Text(
                                        strings.routines.listDeleteButton,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(strings.routines.listBottomBackButton)
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = goBuilder,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(strings.routines.listBottomCreateNewButton)
                }
            }
        }
    }

    if (confirmDelete && pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            confirmButton = {
                Button(
                    onClick = {
                        val id = pendingDeleteId!!
                        confirmDelete = false
                        scope.launch { repo.deleteRoutine(id) }
                    }
                ) {
                    Text(strings.routines.deleteDialogConfirm)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmDelete = false }) {
                    Text(strings.routines.deleteDialogCancel)
                }
            },
            title = { Text(strings.routines.deleteDialogTitle) },
            text = {
                Text(
                    strings.routines.deleteDialogText.replace(
                        "esta rutina",
                        "“$pendingDeleteName”"
                    )
                )
            }
        )
    }
}
