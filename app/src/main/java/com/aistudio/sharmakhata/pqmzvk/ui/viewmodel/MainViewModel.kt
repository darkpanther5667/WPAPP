package com.aistudio.sharmakhata.pqmzvk.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.aistudio.sharmakhata.pqmzvk.data.local.AppDatabase
import com.aistudio.sharmakhata.pqmzvk.data.local.CacheEntry
import com.aistudio.sharmakhata.pqmzvk.data.local.PendingOperation
import com.aistudio.sharmakhata.pqmzvk.data.model.DailyReport
import com.aistudio.sharmakhata.pqmzvk.data.model.FullDatabase
import com.aistudio.sharmakhata.pqmzvk.data.remote.AddCustomerRequest
import com.aistudio.sharmakhata.pqmzvk.data.remote.AddPaymentRequest
import com.aistudio.sharmakhata.pqmzvk.data.remote.ApiClient
import com.aistudio.sharmakhata.pqmzvk.data.remote.ApiService
import com.aistudio.sharmakhata.pqmzvk.data.remote.CreateBillRequest
import com.aistudio.sharmakhata.pqmzvk.data.remote.MarkBillPaidRequest
import com.aistudio.sharmakhata.pqmzvk.data.remote.AddPurchaseRequest
import com.aistudio.sharmakhata.pqmzvk.data.remote.AddExpenseRequest
import com.aistudio.sharmakhata.pqmzvk.data.repository.AuthRepository
import com.aistudio.sharmakhata.pqmzvk.data.repository.AuthResult
import com.aistudio.sharmakhata.pqmzvk.data.sync.DeltaSyncManager
import com.aistudio.sharmakhata.pqmzvk.util.NetworkUtils
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val apiService: ApiService,
    private val moshi: Moshi,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val db = AppDatabase.get(getApplication())
    private val cacheDao = db.cacheDao()
    private val pendingDao = db.pendingDao()

    private val dbAdapter = moshi.adapter(FullDatabase::class.java)
    private val reportAdapter = moshi.adapter(DailyReport::class.java)

    // ══ Collector Job References (prevent duplicate collector leak) ══════════════
    private var dbCollectorJob: kotlinx.coroutines.Job? = null
    private var reportCollectorJob: kotlinx.coroutines.Job? = null
    private var syncErrorCollectorJob: kotlinx.coroutines.Job? = null

    // ══ Generation counter prevents stale data from overwriting fresh data ══════
    // Incremented on each fetchData() call; only writes matching the current
    // generation are applied, so slow cache loads can't overwrite a newer fetch.
    private var fetchGeneration = 0L

    private val _dbState = MutableStateFlow<UiState<FullDatabase>>(UiState.Loading)
    val dbState: StateFlow<UiState<FullDatabase>> = _dbState.asStateFlow()

    private val _reportState = MutableStateFlow<UiState<DailyReport>>(UiState.Loading)
    val reportState: StateFlow<UiState<DailyReport>> = _reportState.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    private val _authToken = MutableStateFlow<String?>(null)
    val authToken: StateFlow<String?> = _authToken.asStateFlow()

    private val _registeredStoreId = MutableStateFlow<String?>(null)
    val registeredStoreId: StateFlow<String?> = _registeredStoreId.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    private val _droppedOps = MutableStateFlow<List<DroppedOperation>>(emptyList())
    val droppedOps: StateFlow<List<DroppedOperation>> = _droppedOps.asStateFlow()

    private val _logoutEvent = MutableStateFlow(false)
    val logoutEvent: StateFlow<Boolean> = _logoutEvent.asStateFlow()

    init {
        viewModelScope.launch {
            ApiClient.unauthorizedEvent.collect {
                _logoutEvent.value = true
            }
        }
    }

    fun consumeLogoutEvent() {
        _logoutEvent.value = false
    }

    // ── Cache-first data loading with delta sync ────────────────────────────────

    fun fetchData(context: android.content.Context? = null) {
        _syncError.value = null
        fetchGeneration++ // invalidate any in-flight operations from prior calls
        val gen = fetchGeneration

        if (context != null) {
            LiveSyncManager.init(context)
        }

        // Cancel previous collector jobs to prevent duplicate collectors
        dbCollectorJob?.cancel()
        reportCollectorJob?.cancel()
        syncErrorCollectorJob?.cancel()

        val ctx = context ?: return

        // Step 1: Check network first
        if (!NetworkUtils.isNetworkAvailable(ctx)) {
            _isOffline.value = true
            _syncError.value = "You're offline — showing saved data"
            // Only show cache when offline
            viewModelScope.launch(Dispatchers.IO) {
                if (gen != fetchGeneration) return@launch
                loadFromCache()
            }
            return
        }
        _isOffline.value = false

        // Step 2: Sync pending offline operations first
        viewModelScope.launch(Dispatchers.IO) {
            if (gen != fetchGeneration) return@launch
            syncPendingOperations()
        }

        // Step 3: Start LiveSyncManager
        LiveSyncManager.start()

        // Collect ongoing LiveSyncManager updates
        dbCollectorJob = viewModelScope.launch {
            LiveSyncManager.fullDatabase.collect { data ->
                if (data != null && gen == fetchGeneration) {
                    _dbState.value = UiState.Success(data)
                    saveDbToCache(data)
                }
            }
        }
        reportCollectorJob = viewModelScope.launch {
            LiveSyncManager.dailyReport.collect { report ->
                if (report != null && gen == fetchGeneration) {
                    _reportState.value = UiState.Success(report)
                    saveReportToCache(report)
                }
            }
        }
        syncErrorCollectorJob = viewModelScope.launch {
            LiveSyncManager.syncError.collect { error ->
                if (error != null && gen == fetchGeneration) {
                    _syncError.value = error
                }
            }
        }

        // Step 4: Always try network first — show loading state
        _dbState.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            if (gen != fetchGeneration) return@launch
            try {
                val response = apiService.getFullDatabase()
                if (response.isSuccessful) {
                    val freshDb = response.body()
                    if (freshDb != null && gen == fetchGeneration) {
                        // Protect against empty DB responses wiping local cache
                        val currentState = _dbState.value
                        val isEmptyResponse = freshDb.customers.isNullOrEmpty() && freshDb.bills.isNullOrEmpty()
                        val hasLocalData = currentState is UiState.Success && 
                            (!currentState.data.customers.isNullOrEmpty() || !currentState.data.bills.isNullOrEmpty())
                        
                        if (isEmptyResponse && hasLocalData) {
                            android.util.Log.w("MainViewModel", "Received empty database response from server, but local cache has data. Ignoring to prevent data loss.")
                            return@launch
                        }

                        _dbState.value = UiState.Success(freshDb)
                        saveDbToCache(freshDb)
                        // Update lastSyncedAt using server time
                        val syncTime = freshDb.serverTime?.let { t ->
                            try { java.time.Instant.parse(t).minusSeconds(2).toString() }
                            catch (e: Exception) { java.time.Instant.now().minusSeconds(2).toString() }
                        } ?: java.time.Instant.now().minusSeconds(2).toString()
                        com.aistudio.sharmakhata.pqmzvk.util.SessionManager.saveLastSyncedAt(ctx, syncTime)
                        return@launch
                    }
                }
            } catch (e: Exception) {
                android.util.Log.d("MainViewModel", "Network fetch failed: ${e.message}")
            }

            // Network failed — fall back to cache
            if (gen == fetchGeneration && _dbState.value !is UiState.Success) {
                loadFromCache()
            }
        }

        // Step 5: Fetch daily report
        viewModelScope.launch(Dispatchers.IO) {
            if (gen != fetchGeneration) return@launch
            try {
                val response = apiService.getDailyReport()
                if (response.isSuccessful) {
                    val freshReport = response.body()
                    if (freshReport != null && gen == fetchGeneration) {
                        _reportState.value = UiState.Success(freshReport)
                        saveReportToCache(freshReport)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.d("MainViewModel", "Report fetch failed: ${e.message}")
            }
        }
    }

    // ── Cache helpers ───────────────────────────────────────────────────────────

    private suspend fun loadFromCache() {
        try {
            val cachedDb = cacheDao.getJson(CACHE_KEY_DB)
            if (cachedDb != null) {
                val parsed = try { dbAdapter.fromJson(cachedDb) } catch (e: Exception) {
                    android.util.Log.w("MainViewModel", "Failed to parse cached FullDatabase: ${e.message}")
                    null
                }
                if (parsed != null) {
                    _dbState.value = UiState.Success(parsed)
                }
            }
            val cachedReport = cacheDao.getJson(CACHE_KEY_REPORT)
            if (cachedReport != null) {
                val parsed = try { reportAdapter.fromJson(cachedReport) } catch (e: Exception) {
                    android.util.Log.w("MainViewModel", "Failed to parse cached report: ${e.message}")
                    null
                }
                if (parsed != null) {
                    _reportState.value = UiState.Success(parsed)
                }
            }
            _pendingCount.value = pendingDao.count()
        } catch (e: Exception) {
            android.util.Log.d("MainViewModel", "Cache load failed: ${e.message}")
        }
    }

    private suspend fun saveDbToCache(data: FullDatabase) {
        try {
            val json = dbAdapter.toJson(data)
            cacheDao.put(CacheEntry(CACHE_KEY_DB, json, System.currentTimeMillis()))

            // Sync purchases and expenses back to local Room database entities
            val dbInstance = AppDatabase.get(getApplication())

            // Sync purchases
            dbInstance.purchaseDao().clearAll()
            data.purchases.forEach { purchase ->
                val mappedItems = purchase.items.map { item ->
                    com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseItemEntry(
                        name = item.name,
                        qty = item.qty.toString(),
                        price = item.price.toString(),
                        amount = item.amount
                    )
                }
                val itemsJson = moshi.adapter<List<com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseItemEntry>>(
                    com.squareup.moshi.Types.newParameterizedType(List::class.java, com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseItemEntry::class.java)
                ).toJson(mappedItems)

                val createdAtLong = try { java.time.Instant.parse(purchase.createdAt).toEpochMilli() } catch (e: Exception) { System.currentTimeMillis() }
                val updatedAtLong = try { java.time.Instant.parse(purchase.updatedAt).toEpochMilli() } catch (e: Exception) { System.currentTimeMillis() }

                dbInstance.purchaseDao().insert(
                    com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseEntity(
                        serverId = purchase.id,
                        supplierName = purchase.supplierName,
                        supplierPhone = purchase.supplierPhone,
                        itemsJson = itemsJson,
                        totalAmount = purchase.totalAmount,
                        paidAmount = purchase.paidAmount,
                        status = purchase.status,
                        notes = purchase.notes,
                        createdAt = createdAtLong,
                        updatedAt = updatedAtLong
                    )
                )
            }

            // Sync expenses
            dbInstance.expenseDao().clearAll()
            data.expenses.forEach { expense ->
                val createdAtLong = try { java.time.Instant.parse(expense.createdAt).toEpochMilli() } catch (e: Exception) { System.currentTimeMillis() }
                dbInstance.expenseDao().insert(
                    com.aistudio.sharmakhata.pqmzvk.data.local.ExpenseEntity(
                        serverId = expense.id,
                        title = expense.title,
                        amount = expense.amount,
                        category = expense.category,
                        note = expense.note,
                        createdAt = createdAtLong
                    )
                )
            }
        } catch (e: Exception) {
            android.util.Log.d("MainViewModel", "Cache save failed: ${e.message}")
        }
    }

    private suspend fun saveReportToCache(data: DailyReport) {
        try {
            val json = reportAdapter.toJson(data)
            cacheDao.put(CacheEntry(CACHE_KEY_REPORT, json, System.currentTimeMillis()))
        } catch (e: Exception) {
            android.util.Log.d("MainViewModel", "Report cache save failed: ${e.message}")
        }
    }

    // ── Offline operation queue ─────────────────────────────────────────────────

    suspend fun queueOperation(type: String, payload: String) {
        pendingDao.insert(PendingOperation(type = type, payload = payload))
        _pendingCount.value = pendingDao.count()
    }

    suspend fun syncPendingOperations() {
        val pending = pendingDao.getAll().sortedBy { it.createdAt }
        if (pending.isEmpty()) return

        val newDropped = mutableListOf<DroppedOperation>()

        for (op in pending) {
            if (op.retries >= MAX_RETRIES) {
                val drop = DroppedOperation(
                    type = op.type,
                    payload = op.payload,
                    error = "Exceeded $MAX_RETRIES retries — dropped after repeated failures"
                )
                newDropped.add(drop)
                pendingDao.delete(op.id)
                android.util.Log.w("SyncManager", "Dropping operation ${op.type} (id=${op.id}) after $MAX_RETRIES failed retries")
                _syncError.value = "A pending ${op.type} was dropped after $MAX_RETRIES failed attempts — data may be incomplete"
                continue
            }

            try {
                val success = when (op.type) {
                    "add_customer" -> {
                        val req = moshi.adapter(AddCustomerRequest::class.java).fromJson(op.payload)
                        if (req != null) {
                            val response = apiService.addCustomer(req)
                            if (response.code() == 409) {
                                true
                            } else {
                                response.isSuccessful && response.body()?.success == true
                            }
                        } else false
                    }
                    "create_bill" -> {
                        val req = moshi.adapter(CreateBillRequest::class.java).fromJson(op.payload)
                        if (req != null) {
                            val response = apiService.createBill(req)
                            response.isSuccessful && response.body()?.success == true
                        } else false
                    }
                    "add_payment" -> {
                        val req = moshi.adapter(AddPaymentRequest::class.java).fromJson(op.payload)
                        if (req != null) {
                            val response = apiService.addPayment(req)
                            response.isSuccessful
                        } else false
                    }
                    "mark_paid" -> {
                        val req = moshi.adapter(MarkBillPaidRequest::class.java).fromJson(op.payload)
                        if (req != null) {
                            val response = apiService.markBillPaid(req)
                            response.isSuccessful && response.body()?.success == true
                        } else false
                    }
                    "add_purchase" -> {
                        val req = moshi.adapter(AddPurchaseRequest::class.java).fromJson(op.payload)
                        if (req != null) {
                            val response = apiService.addPurchase(req)
                            response.isSuccessful && response.body()?.success == true
                        } else false
                    }
                    "delete_purchase" -> {
                        val response = apiService.deletePurchase(op.payload)
                        response.isSuccessful && response.body()?.success == true
                    }
                    "add_expense" -> {
                        val req = moshi.adapter(AddExpenseRequest::class.java).fromJson(op.payload)
                        if (req != null) {
                            val response = apiService.addExpense(req)
                            response.isSuccessful && response.body()?.success == true
                        } else false
                    }
                    "delete_expense" -> {
                        val response = apiService.deleteExpense(op.payload)
                        response.isSuccessful && response.body()?.success == true
                    }
                    else -> false
                }

                if (success) {
                    pendingDao.delete(op.id)
                } else {
                    android.util.Log.d("SyncManager", "Operation ${op.type} failed, keeping for retry")
                }
            } catch (e: Exception) {
                pendingDao.incrementRetries(op.id)
                android.util.Log.d("SyncManager", "Operation ${op.type} failed (retry ${op.retries + 1}/$MAX_RETRIES): ${e.message}")
            }
        }

        _pendingCount.value = pendingDao.count()
        if (newDropped.isNotEmpty()) {
            _droppedOps.value = _droppedOps.value + newDropped
        }
        LiveSyncManager.requestImmediateSync()
    }

    // ── LiveSync ────────────────────────────────────────────────────────────────

    fun startLiveSyncIfNeeded(context: android.content.Context? = null) {
        if (_authToken.value != null) {
            if (context != null) LiveSyncManager.init(context)
            LiveSyncManager.start()
        }
    }

    fun setSyncInterval(millis: Long) {
        LiveSyncManager.intervalMillis = millis
    }

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }

    // ── Auth (delegated to AuthRepository) ──────────────────────────────────────

    fun requestLoginCode(phone: String, storeId: String? = null, retryCount: Int = 0) {
        val sid = storeId.takeIf { !it.isNullOrBlank() } ?: com.aistudio.sharmakhata.pqmzvk.util.SessionManager.storeId ?: ""
        _operationState.value = OperationState.Loading
        viewModelScope.launch {
            val result = authRepository.requestLoginCode(sid, phone)
            when (result) {
                is AuthResult.Success -> _operationState.value = OperationState.Success(result.message ?: "Code sent")
                is AuthResult.Error -> _operationState.value = OperationState.Error(result.message)
            }
        }
    }

    fun verifyLoginCode(phone: String, code: String, context: android.content.Context, storeId: String? = null) {
        val sid = storeId.takeIf { !it.isNullOrBlank() } ?: com.aistudio.sharmakhata.pqmzvk.util.SessionManager.storeId ?: ""
        _operationState.value = OperationState.Loading
        viewModelScope.launch {
            val result = authRepository.verifyLoginCode(sid, phone, code)
            when (result) {
                is AuthResult.Success -> {
                    result.token?.let { token ->
                        _authToken.value = token
                        com.aistudio.sharmakhata.pqmzvk.util.SessionManager.setToken(context, token)
                    }
                    result.storeId?.let { id ->
                        val oldStoreId = com.aistudio.sharmakhata.pqmzvk.util.SessionManager.storeId
                        if (oldStoreId != null && oldStoreId != id) {
                            db.cacheDao().clearAll()
                            db.pendingDao().clearAll()
                        }
                        com.aistudio.sharmakhata.pqmzvk.util.SessionManager.saveStoreInfo(context, id, phone)
                        com.aistudio.sharmakhata.pqmzvk.util.SessionManager.reload(context)
                    }
                    LiveSyncManager.stop()
                    LiveSyncManager.init(context)
                    LiveSyncManager.start()
                    fetchData(context)
                    _operationState.value = OperationState.Success("Logged in")
                }
                is AuthResult.Error -> _operationState.value = OperationState.Error(result.message)
            }
        }
    }

    fun loginWithPassword(phone: String, password: String, context: android.content.Context) {
        _operationState.value = OperationState.Loading
        viewModelScope.launch {
            val result = authRepository.loginWithPassword(phone, password)
            when (result) {
                is AuthResult.Success -> {
                    result.token?.let { token ->
                        _authToken.value = token
                        com.aistudio.sharmakhata.pqmzvk.util.SessionManager.setToken(context, token)
                    }
                    result.storeId?.let { id ->
                        val oldStoreId = com.aistudio.sharmakhata.pqmzvk.util.SessionManager.storeId
                        if (oldStoreId != null && oldStoreId != id) {
                            db.cacheDao().clearAll()
                            db.pendingDao().clearAll()
                        }
                        com.aistudio.sharmakhata.pqmzvk.util.SessionManager.saveStoreInfo(context, id, phone)
                        com.aistudio.sharmakhata.pqmzvk.util.SessionManager.reload(context)
                    }
                    LiveSyncManager.stop()
                    LiveSyncManager.init(context)
                    LiveSyncManager.start()
                    fetchData(context)
                    _operationState.value = OperationState.Success("Logged in")
                }
                is AuthResult.Error -> _operationState.value = OperationState.Error(result.message)
            }
        }
    }

    fun loginWithGoogle(idToken: String, context: android.content.Context) {
        _operationState.value = OperationState.Loading
        viewModelScope.launch {
            val result = authRepository.googleSignIn(idToken)
            when (result) {
                is AuthResult.Success -> {
                    result.token?.let { token ->
                        _authToken.value = token
                        com.aistudio.sharmakhata.pqmzvk.util.SessionManager.setToken(context, token)
                    }
                    result.storeId?.let { id ->
                        // Clear old cache if store changed
                        val oldStoreId = com.aistudio.sharmakhata.pqmzvk.util.SessionManager.storeId
                        if (oldStoreId != null && oldStoreId != id) {
                            db.cacheDao().clearAll()
                            db.pendingDao().clearAll()
                        }
                        com.aistudio.sharmakhata.pqmzvk.util.SessionManager.saveStoreInfo(context, id, "")
                        com.aistudio.sharmakhata.pqmzvk.util.SessionManager.reload(context)
                    }
                    LiveSyncManager.stop()
                    LiveSyncManager.init(context)
                    LiveSyncManager.start()
                    fetchData(context) // Force full refresh after login
                    val msg = if (result.isNewUser == true) "Register" else "Logged in"
                    _operationState.value = OperationState.Success(msg)
                }
                is AuthResult.Error -> _operationState.value = OperationState.Error(result.message)
            }
        }
    }

    fun consumeAuthToken(): String? {
        val t = _authToken.value
        _authToken.value = null
        return t
    }

    fun consumeRegisteredStoreId(): String? {
        val id = _registeredStoreId.value
        _registeredStoreId.value = null
        return id
    }

    fun registerStore(
        storeName: String,
        ownerName: String,
        phone: String,
        email: String,
        address: String?,
        gstin: String? = null,
        password: String? = null,
        context: android.content.Context? = null,
    ) {
        _operationState.value = OperationState.Loading
        viewModelScope.launch {
            val result = authRepository.registerStore(storeName, ownerName, phone, email, address, gstin, password)
            when (result) {
                is AuthResult.Success -> {
                    result.storeId?.let { id ->
                        _registeredStoreId.value = id
                        if (context != null) {
                            com.aistudio.sharmakhata.pqmzvk.util.SessionManager.saveStoreInfo(context, id, phone)
                            com.aistudio.sharmakhata.pqmzvk.util.SessionManager.reload(context)
                        }
                    }
                    _operationState.value = OperationState.Success(result.message ?: "Store registered")
                }
                is AuthResult.Error -> _operationState.value = OperationState.Error(result.message)
            }
        }
    }

    fun updateStoreProfile(
        storeName: String,
        ownerName: String,
        address: String?,
        upiId: String?,
        gstin: String?,
        invoiceTemplate: String?,
        context: android.content.Context
    ) {
        _operationState.value = OperationState.Loading
        viewModelScope.launch {
            val result = authRepository.updateStoreProfile(storeName, ownerName, address, upiId, gstin, invoiceTemplate)
            when (result) {
                is AuthResult.Success -> {
                    _operationState.value = OperationState.Success(result.message ?: "Updated")
                    LiveSyncManager.forceRefresh()
                    fetchData(context)
                }
                is AuthResult.Error -> _operationState.value = OperationState.Error(result.message)
            }
        }
    }

    fun updateInvoiceTemplate(templateName: String, context: android.content.Context) {
        val currentDb = (dbState.value as? UiState.Success)?.data ?: return
        val currentShop = currentDb.shop ?: return
        updateStoreProfile(
            storeName = currentShop.name ?: "",
            ownerName = currentShop.owner ?: "",
            address = currentShop.address,
            upiId = currentShop.upiId,
            gstin = currentShop.gstin,
            invoiceTemplate = templateName,
            context = context
        )
    }

    companion object {
        const val CACHE_KEY_DB = "full_database"
        const val CACHE_KEY_REPORT = "daily_report"
        const val MAX_RETRIES = 3
    }
}
