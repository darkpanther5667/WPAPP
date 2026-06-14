package com.aistudio.sharmakhata.pqmzvk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.sharmakhata.pqmzvk.data.local.ItemEntity
import com.aistudio.sharmakhata.pqmzvk.data.remote.StoredItem
import com.aistudio.sharmakhata.pqmzvk.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<ItemEntity>>(emptyList())
    val items: StateFlow<List<ItemEntity>> = _items.asStateFlow()

    private val _storedItems = MutableStateFlow<List<StoredItem>>(emptyList())
    val storedItems: StateFlow<List<StoredItem>> = _storedItems.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    private var itemsJob: Job? = null

    fun loadItems() {
        itemsJob?.cancel()
        itemsJob = viewModelScope.launch {
            _isLoading.value = true
            inventoryRepository.getAllItems().collect { _items.value = it }
            _isLoading.value = false
        }
    }

    fun saveItem(name: String, price: Double, stock: Int, lowStockAlert: Int, hsnCode: String = "", itemId: Long? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            if (itemId != null) {
                inventoryRepository.update(ItemEntity(id = itemId, name = name, price = price, hsnCode = hsnCode, stock = stock, lowStockAlert = lowStockAlert))
            } else {
                inventoryRepository.insert(ItemEntity(name = name, price = price, hsnCode = hsnCode, stock = stock, lowStockAlert = lowStockAlert))
            }
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch(Dispatchers.IO) { inventoryRepository.deleteById(id) }
    }

    fun refreshItems() {
        viewModelScope.launch(Dispatchers.IO) {
            inventoryRepository.refreshFromServer()
        }
    }

    fun loadStoredItems() {
        viewModelScope.launch(Dispatchers.IO) {
            _storedItems.value = inventoryRepository.loadStoredItems()
        }
    }

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }
}
