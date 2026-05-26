package com.aistudio.sharmakhata.pqmzvk.ui.viewmodel

import com.aistudio.sharmakhata.pqmzvk.data.model.DailyReport
import com.aistudio.sharmakhata.pqmzvk.data.model.FullDatabase
import com.aistudio.sharmakhata.pqmzvk.data.remote.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant

object LiveSyncManager {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var syncJob: Job? = null

    // Default interval 5 seconds (configurable via settings later)
    var intervalMillis: Long = 5_000L

    private val _dailyReport = MutableStateFlow<DailyReport?>(null)
    val dailyReport: StateFlow<DailyReport?> = _dailyReport.asStateFlow()

    private val _fullDatabase = MutableStateFlow<FullDatabase?>(null)
    val fullDatabase: StateFlow<FullDatabase?> = _fullDatabase.asStateFlow()

    private val _lastSynced = MutableStateFlow<Instant?>(null)
    val lastSynced: StateFlow<Instant?> = _lastSynced.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    fun start() {
        if (syncJob != null) return // already running
        _syncError.value = null // Clear any previous errors
        syncJob = scope.launch {
            while (isActive) {
                try {
                    val report = ApiClient.apiService.getDailyReport()
                    val db = ApiClient.apiService.getFullDatabase()
                    _dailyReport.value = report
                    _fullDatabase.value = db
                    _lastSynced.value = Instant.now()
                    _syncError.value = null // Clear error on successful sync
                } catch (e: Exception) {
                    // Log error and notify UI
                    _syncError.value = "Sync failed: ${e.message}"
                    println("LiveSyncManager error: ${e.message}")
                }
                delay(intervalMillis)
            }
        }
    }

    fun stop() {
        syncJob?.cancel()
        syncJob = null
        _syncError.value = null
    }
}
