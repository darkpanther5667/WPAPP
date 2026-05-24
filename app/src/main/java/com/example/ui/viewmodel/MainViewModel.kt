package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DailyReport
import com.example.data.model.FullDatabase
import com.example.data.remote.AddCustomerRequest
import com.example.data.remote.AddPaymentRequest
import com.example.data.remote.ApiClient
import com.example.data.remote.BillItemRequest
import com.example.data.remote.CreateBillRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

sealed class OperationState {
    object Idle : OperationState()
    object Loading : OperationState()
    data class Success(val message: String) : OperationState()
    data class Error(val message: String) : OperationState()
}

class MainViewModel : ViewModel() {
    private val _dbState = MutableStateFlow<UiState<FullDatabase>>(UiState.Loading)
    val dbState: StateFlow<UiState<FullDatabase>> = _dbState.asStateFlow()

    private val _reportState = MutableStateFlow<UiState<DailyReport>>(UiState.Loading)
    val reportState: StateFlow<UiState<DailyReport>> = _reportState.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        _dbState.value = UiState.Loading
        _reportState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val db = ApiClient.apiService.getFullDatabase()
                _dbState.value = UiState.Success(db)
            } catch (e: Exception) {
                _dbState.value = UiState.Error("Failed to load database: ${e.message}")
            }

            try {
                val report = ApiClient.apiService.getDailyReport()
                _reportState.value = UiState.Success(report)
            } catch (e: Exception) {
                _reportState.value = UiState.Error("Failed to load report: ${e.message}")
            }
        }
    }

    fun addCustomer(name: String, phone: String) {
        _operationState.value = OperationState.Loading
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.addCustomer(AddCustomerRequest(name, phone))
                if (response.isSuccessful && response.body()?.success == true) {
                    _operationState.value = OperationState.Success("Customer added successfully")
                    fetchData() // Refresh data
                } else {
                    _operationState.value = OperationState.Error(response.body()?.message ?: "Failed to add customer")
                }
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed to add customer: ${e.message}")
            }
        }
    }

    fun addPayment(customerId: String, amount: Double, note: String?) {
        _operationState.value = OperationState.Loading
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.addPayment(AddPaymentRequest(customerId, amount, note))
                if (response.isSuccessful) {
                    _operationState.value = OperationState.Success("Payment recorded successfully")
                    fetchData() // Refresh data
                } else {
                    _operationState.value = OperationState.Error("Failed to record payment")
                }
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed to record payment: ${e.message}")
            }
        }
    }

    fun createBill(customerId: String, amount: Double, items: List<BillItemRequest>?) {
        _operationState.value = OperationState.Loading
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.createBill(CreateBillRequest(customerId, amount, items))
                if (response.isSuccessful) {
                    _operationState.value = OperationState.Success("Bill created successfully")
                    fetchData() // Refresh data
                } else {
                    _operationState.value = OperationState.Error("Failed to create bill")
                }
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed to create bill: ${e.message}")
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }
}
