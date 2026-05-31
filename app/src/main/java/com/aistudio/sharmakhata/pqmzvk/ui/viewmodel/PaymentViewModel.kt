package com.aistudio.sharmakhata.pqmzvk.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.sharmakhata.pqmzvk.data.model.FullDatabase
import com.aistudio.sharmakhata.pqmzvk.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    val dbState: StateFlow<UiState<FullDatabase>> = LiveSyncManager.fullDatabase
        .map { db ->
            if (db == null) UiState.Loading
            else UiState.Success(db)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    fun addPayment(
        context: Context,
        customerId: String,
        amount: Double,
        note: String?,
        paymentMode: String = "cash",
        type: String = "payment"
    ) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val result = paymentRepository.addPayment(context, customerId, amount, note, paymentMode, type)
            _operationState.value = when (result) {
                is RepoResult.Success -> OperationState.Success(result.message)
                is RepoResult.Error -> OperationState.Error(result.message)
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }
}
