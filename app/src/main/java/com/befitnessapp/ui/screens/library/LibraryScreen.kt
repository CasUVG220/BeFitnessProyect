package com.befitnessapp.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.befitnessapp.domain.catalog.Catalogo
import com.befitnessapp.domain.catalog.Exercise

@Composable
fun LibraryScreen(onBack: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedGroupId by rememberSaveable { mutableStateOf<Int?>(null) }

    val results by remember(query, selectedGroupId) {
        mutableStateOf(
            Catalogo.searchExercises(
                query = query,
                groupId = selectedGroupId
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Biblioteca de ejercicios", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onBack) { Text("Volver") }
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Buscar") },
            modifier = Modifier.fillMaxWidth()
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            item {
                FilterChip(
                    text = "Todos",
                    selected = selectedGroupId == null,
                    onClick = { selectedGroupId = null }
                )
            }
            items(Catalogo.allGroups) { grp ->
                FilterChip(
                    text = grp.name,
                    selected = selectedGroupId == grp.id,
                    onClick = {
                        selectedGroupId = if (selectedGroupId == grp.id) null else grp.id
                    }
                )
            }
        }

        if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No se encontraron ejercicios.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results) { ex ->
                    ExerciseCard(ex)
                }
            }
        }
    }
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        tonalElevation = if (selected) 2.dp else 0.dp,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .heightIn(min = 36.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ExerciseCard(ex: Exercise) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(ex.name, style = MaterialTheme.typography.titleMedium)
            Text("Patrón: ${ex.pattern}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
