package com.aistudio.sharmakhata.pqmzvk.data.repository

import com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseDao
import com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PurchaseRepository @Inject constructor(
    private val purchaseDao: PurchaseDao
) {
    fun getAllPurchases(): Flow<List<PurchaseEntity>> = purchaseDao.getAllPurchases()

    suspend fun insert(purchase: PurchaseEntity) {
        purchaseDao.insert(purchase)
    }

    suspend fun deleteById(id: Long) {
        purchaseDao.deleteById(id)
    }
}
