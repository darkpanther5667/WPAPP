package com.aistudio.sharmakhata.pqmzvk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.aistudio.sharmakhata.pqmzvk.data.model.Bill
import com.aistudio.sharmakhata.pqmzvk.data.model.Customer
import com.aistudio.sharmakhata.pqmzvk.data.model.FullDatabase
import com.aistudio.sharmakhata.pqmzvk.data.model.Transaction
import com.aistudio.sharmakhata.pqmzvk.data.repository.CustomerRepository
import com.aistudio.sharmakhata.pqmzvk.ui.common.InMemoryPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    val dbState: StateFlow<UiState<FullDatabase>> = LiveSyncManager.fullDatabase
        .map { db ->
            if (db == null) UiState.Loading
            else UiState.Success(db)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val customers: StateFlow<List<Customer>> = dbState.map { state ->
        when (state) {
            is UiState.Success -> state.data.customers
            else -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filter = MutableStateFlow("All")
    val filter: StateFlow<String> = _filter.asStateFlow()

    fun setFilter(value: String) { _filter.value = value }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(value: String) { _searchQuery.value = value }

    val customersPagingData: Flow<PagingData<Customer>> = combine(dbState, _filter, _searchQuery) { state, filter, search ->
        when (state) {
            is UiState.Success -> {
                val allCustomers = state.data.customers
                val transactions = state.data.transactions
                val bills = state.data.bills
                val searchLower = search.lowercase()
                allCustomers.filter { customer ->
                    val matchesSearch = searchLower.isEmpty() ||
                        customer.name.lowercase().contains(searchLower) ||
                        (customer.phone?.lowercase()?.contains(searchLower) == true)
                    val balance = outstandingBalance(customer.id, transactions, bills)
                    val matchesFilter = when (filter) {
                        "With Outstanding" -> balance > 0
                        "Paid" -> balance <= 0
                        else -> true
                    }
                    matchesSearch && matchesFilter
                }
            }
            else -> emptyList()
        }
    }.flatMapLatest { filtered ->
        Pager(PagingConfig(pageSize = 20)) {
            InMemoryPagingSource(filtered)
        }.flow
    }.cachedIn(viewModelScope)

    private fun outstandingBalance(customerId: String, transactions: List<Transaction>, bills: List<Bill>): Double {
        val payments = transactions.filter { it.customerId == customerId && it.type == "payment" }.sumOf { it.amount }
        val credits = transactions.filter { it.customerId == customerId && it.type == "credit" }.sumOf { it.amount }
        val unpaidBillTotal = bills.filter { it.customerId == customerId && it.status != "paid" }.sumOf { it.total }
        return credits + unpaidBillTotal - payments
    }

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    fun addCustomer(name: String, phone: String, context: android.content.Context) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val result = customerRepository.addCustomer(name, phone, context)
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
