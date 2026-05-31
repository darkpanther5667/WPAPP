package com.aistudio.sharmakhata.pqmzvk.ui.viewmodel

import android.content.Context
import com.aistudio.sharmakhata.pqmzvk.data.model.DailyReport
import com.aistudio.sharmakhata.pqmzvk.data.model.FullDatabase
import com.aistudio.sharmakhata.pqmzvk.data.remote.ApiClient
import com.aistudio.sharmakhata.pqmzvk.data.sync.DeltaSyncManager
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
    private var contextRef: Context? = null
    private var consecutiveFailures = 0
    private val maxConsecutiveFailures = 3

    var intervalMillis: Long = 30_000L

    private val _dailyReport = MutableStateFlow<DailyReport?>(null)
    val dailyReport: StateFlow<DailyReport?> = _dailyReport.asStateFlow()

    private val _fullDatabase = MutableStateFlow<FullDatabase?>(null)
    val fullDatabase: StateFlow<FullDatabase?> = _fullDatabase.asStateFlow()

    private val _lastSynced = MutableStateFlow<Instant?>(null)
    val lastSynced: StateFlow<Instant?> = _lastSynced.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    fun init(context: Context) {
        contextRef = context
    }

    fun start() {
        if (syncJob != null) return
        val ctx = contextRef ?: run {
            android.util.Log.w("LiveSyncManager", "Cannot start — not initialized with context")
            return
        }
        android.util.Log.d("LiveSyncManager", "Starting delta sync with interval ${intervalMillis}ms")
        _syncError.value = null
        consecutiveFailures = 0
        syncJob = scope.launch {
            android.util.Log.d("LiveSyncManager", "Delta sync loop started")
            while (isActive) {
                try {
                    // Use DeltaSyncManager for the database
                    val db = DeltaSyncManager.fetch(ctx, _fullDatabase.value)
                    if (db != null) {
                        _fullDatabase.value = db
                        _lastSynced.value = Instant.now()
                        _syncError.value = null
                        consecutiveFailures = 0
                        DeltaSyncManager.resetFailures()
                        android.util.Log.d("LiveSyncManager", "DB sync successful")
                    } else {
                        consecutiveFailures++
                        val errorMsg = "Sync failed ($consecutiveFailures/$maxConsecutiveFailures)"
                        _syncError.value = errorMsg
                        android.util.Log.w("LiveSyncManager", errorMsg)

                        val backoffMs = DeltaSyncManager.getBackoffMs().coerceAtLeast(intervalMillis)
                        android.util.Log.d("LiveSyncManager", "Backoff ${backoffMs}ms")
                        delay(backoffMs)
                        continue
                    }

                    // Daily report is a lightweight aggregation — always fetch fresh
                    try {
                        val reportResponse = ApiClient.apiService.getDailyReport()
                        if (reportResponse.isSuccessful) {
                            _dailyReport.value = reportResponse.body()
                        }
                    } catch (e: Exception) {
                        android.util.Log.d("LiveSyncManager", "Report fetch failed: ${e.message}")
                    }

                } catch (e: Exception) {
                    consecutiveFailures++
                    _syncError.value = "Sync failed — will retry"
                    android.util.Log.e("LiveSyncManager", "Sync loop error: ${e.message}")
                    val backoffMs = (intervalMillis * consecutiveFailures).coerceAtMost(300_000L)
                    delay(backoffMs)
                    continue
                }
                delay(intervalMillis)
            }
        }
    }

    fun stop() {
        syncJob?.cancel()
        syncJob = null
        _syncError.value = null
        consecutiveFailures = 0
        DeltaSyncManager.resetFailures()
    }

    suspend fun forceRefresh() {
        val ctx = contextRef ?: return
        try {
            val db = DeltaSyncManager.fetch(ctx, _fullDatabase.value)
            if (db != null) {
                _fullDatabase.value = db
                _lastSynced.value = Instant.now()
                _syncError.value = null
                consecutiveFailures = 0
                DeltaSyncManager.resetFailures()
            }
            val reportResponse = ApiClient.apiService.getDailyReport()
            if (reportResponse.isSuccessful) {
                _dailyReport.value = reportResponse.body()
            }
        } catch (e: Exception) {
            android.util.Log.d("LiveSyncManager", "forceRefresh error: ${e.message}")
        }
    }
}
