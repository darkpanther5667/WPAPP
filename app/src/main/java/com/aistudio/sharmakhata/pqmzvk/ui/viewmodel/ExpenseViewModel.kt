package com.aistudio.sharmakhata.pqmzvk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.sharmakhata.pqmzvk.data.local.ExpenseEntity
import com.aistudio.sharmakhata.pqmzvk.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _expenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    val expenses: StateFlow<List<ExpenseEntity>> = _expenses.asStateFlow()

    private val _todayTotal = MutableStateFlow(0.0)
    val todayTotal: StateFlow<Double> = _todayTotal.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    private var expensesJob: Job? = null

    fun loadExpenses() {
        expensesJob?.cancel()
        expensesJob = viewModelScope.launch {
            expenseRepository.getAllExpenses().collect { _expenses.value = it }
        }
    }

    fun loadTodayTotal() {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            val startOfDay = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
            val endOfDay = cal.timeInMillis
            expenseRepository.getTotalExpensesBetween(startOfDay, endOfDay).collect { _todayTotal.value = it ?: 0.0 }
        }
    }

    fun saveExpense(title: String, amount: Double, category: String, note: String?, context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _operationState.value = OperationState.Loading
            val result = expenseRepository.addExpense(title, amount, category, note, context)
            withContext(Dispatchers.Main) {
                if (result is RepoResult.Success) {
                    _operationState.value = OperationState.Success(result.message)
                } else if (result is RepoResult.Error) {
                    _operationState.value = OperationState.Error(result.message)
                }
            }
        }
    }

    fun deleteExpense(id: Long, serverId: String, context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _operationState.value = OperationState.Loading
            val result = expenseRepository.deleteExpense(id, serverId, context)
            withContext(Dispatchers.Main) {
                if (result is RepoResult.Success) {
                    _operationState.value = OperationState.Success(result.message)
                } else if (result is RepoResult.Error) {
                    _operationState.value = OperationState.Error(result.message)
                }
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }
}
