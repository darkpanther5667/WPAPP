package com.aistudio.sharmakhata.pqmzvk.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseEntity
import com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseItemEntry
import com.aistudio.sharmakhata.pqmzvk.data.repository.PurchaseRepository
import com.aistudio.sharmakhata.pqmzvk.ui.screens.InvoiceTemplate
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {

    private val _purchases = MutableStateFlow<List<PurchaseEntity>>(emptyList())
    val purchases: StateFlow<List<PurchaseEntity>> = _purchases.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    private val _currentInvoiceTemplate = MutableStateFlow(InvoiceTemplate.MODERN)
    val currentInvoiceTemplate: StateFlow<InvoiceTemplate> = _currentInvoiceTemplate.asStateFlow()

    private var purchasesJob: Job? = null

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val purchaseItemsAdapter = moshi.adapter<List<PurchaseItemEntry>>(
        com.squareup.moshi.Types.newParameterizedType(List::class.java, PurchaseItemEntry::class.java)
    )

    fun loadPurchases() {
        purchasesJob?.cancel()
        purchasesJob = viewModelScope.launch {
            purchaseRepository.getAllPurchases().collect { _purchases.value = it }
        }
    }

    fun savePurchase(supplierName: String, supplierPhone: String, items: List<PurchaseItemEntry>, totalAmount: Double, paidAmount: Double, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val itemsJson = purchaseItemsAdapter.toJson(items)
                purchaseRepository.insert(
                    PurchaseEntity(
                        supplierName = supplierName,
                        supplierPhone = supplierPhone,
                        itemsJson = itemsJson,
                        totalAmount = totalAmount,
                        paidAmount = paidAmount,
                        notes = notes,
                        status = if (paidAmount >= totalAmount) "paid" else if (paidAmount > 0) "partial" else "unpaid"
                    )
                )
                _operationState.value = OperationState.Success("Purchase recorded")
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed to save purchase: ${e.message}")
            }
        }
    }

    fun deletePurchase(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                purchaseRepository.deleteById(id)
                _operationState.value = OperationState.Success("Purchase deleted")
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed to delete purchase: ${e.message}")
            }
        }
    }

    fun setInvoiceTemplate(template: InvoiceTemplate) {
        _currentInvoiceTemplate.value = template
    }

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }
}
