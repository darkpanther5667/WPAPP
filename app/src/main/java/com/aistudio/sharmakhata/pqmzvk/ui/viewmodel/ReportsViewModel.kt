package com.aistudio.sharmakhata.pqmzvk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.sharmakhata.pqmzvk.data.model.DailyReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor() : ViewModel() {

    val reportState: StateFlow<UiState<DailyReport>> = LiveSyncManager.dailyReport
        .map { report ->
            if (report == null) UiState.Loading
            else UiState.Success(report)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val dbState: StateFlow<UiState<com.aistudio.sharmakhata.pqmzvk.data.model.FullDatabase>> =
        LiveSyncManager.fullDatabase
            .map { db ->
                if (db == null) UiState.Loading
                else UiState.Success(db)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)
}
