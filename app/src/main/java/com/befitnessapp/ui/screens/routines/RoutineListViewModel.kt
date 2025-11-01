package com.befitnessapp.ui.screens.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.befitnessapp.data.repository.RoutineRepository
import com.befitnessapp.domain.routines.RoutineDetail
import com.befitnessapp.domain.routines.usecases.ArchiveRoutineUC
import com.befitnessapp.domain.routines.usecases.ObserveRoutinesUC
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RoutineListUiState(val routines: List<RoutineDetail> = emptyList())

class RoutineListViewModel(
    observeRoutinesUC: ObserveRoutinesUC,
    private val archiveRoutineUC: ArchiveRoutineUC
) : ViewModel() {

    val uiState: StateFlow<RoutineListUiState> =
        observeRoutinesUC()
            .map { RoutineListUiState(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RoutineListUiState())

    fun archive(id: Long) {
        viewModelScope.launch { archiveRoutineUC(id) }
    }

    companion object {
        fun factory(repo: RoutineRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val observe = com.befitnessapp.domain.routines.usecases.ObserveRoutinesUC(repo)
                val archive = com.befitnessapp.domain.routines.usecases.ArchiveRoutineUC(repo)
                return RoutineListViewModel(observe, archive) as T
            }
        }
    }
}
