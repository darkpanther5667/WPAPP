package com.aistudio.sharmakhata.pqmzvk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.sharmakhata.pqmzvk.data.model.FullDatabase
import com.aistudio.sharmakhata.pqmzvk.data.model.Transaction
import com.aistudio.sharmakhata.pqmzvk.data.remote.ApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {

    val dbState: StateFlow<UiState<FullDatabase>> = LiveSyncManager.fullDatabase
        .map { db ->
            if (db == null) UiState.Loading
            else UiState.Success(db)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val reportState: StateFlow<UiState<com.aistudio.sharmakhata.pqmzvk.data.model.DailyReport>> =
        LiveSyncManager.dailyReport
            .map { report ->
                if (report == null) UiState.Loading
                else UiState.Success(report)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val syncError: StateFlow<String?> = LiveSyncManager.syncError
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isOffline: StateFlow<Boolean> = LiveSyncManager.syncError.map { error ->
        error != null && error.contains("offline", ignoreCase = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        // Unauthorized events handled globally in MainViewModel
    }
}
