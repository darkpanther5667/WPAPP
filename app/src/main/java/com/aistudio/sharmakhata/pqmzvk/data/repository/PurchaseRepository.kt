package com.aistudio.sharmakhata.pqmzvk.data.repository

import com.aistudio.sharmakhata.pqmzvk.data.local.PendingDao
import com.aistudio.sharmakhata.pqmzvk.data.local.PendingOperation
import com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseDao
import com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseEntity
import com.aistudio.sharmakhata.pqmzvk.data.remote.AddPurchaseRequest
import com.aistudio.sharmakhata.pqmzvk.data.remote.PurchaseItemRequest
import com.aistudio.sharmakhata.pqmzvk.data.remote.ApiService
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.RepoResult
import com.aistudio.sharmakhata.pqmzvk.util.NetworkUtils
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseRepository @Inject constructor(
    private val purchaseDao: PurchaseDao,
    private val pendingDao: PendingDao,
    private val apiService: ApiService,
    private val moshi: Moshi
) {
    fun getAllPurchases(): Flow<List<PurchaseEntity>> = purchaseDao.getAllPurchases()

    suspend fun addPurchase(supplierName: String, supplierPhone: String, items: List<com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseItemEntry>, totalAmount: Double, paidAmount: Double, notes: String, context: android.content.Context): RepoResult {
        val mappedItems = items.map { item ->
            PurchaseItemRequest(
                name = item.name,
                qty = item.qty.toIntOrNull() ?: 1,
                price = item.price.toDoubleOrNull() ?: 0.0,
                amount = item.amount
            )
        }
        val req = AddPurchaseRequest(
            supplierName = supplierName,
            supplierPhone = supplierPhone,
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            status = if (paidAmount >= totalAmount) "paid" else if (paidAmount > 0) "partial" else "unpaid",
            items = mappedItems,
            notes = notes
        )
        return try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                val payload = moshi.adapter(AddPurchaseRequest::class.java).toJson(req)
                pendingDao.insert(PendingOperation(type = "add_purchase", payload = payload))
                val itemsJson = moshi.adapter<List<com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseItemEntry>>(
                    com.squareup.moshi.Types.newParameterizedType(List::class.java, com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseItemEntry::class.java)
                ).toJson(items)
                purchaseDao.insert(com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseEntity(
                    serverId = "local_${System.currentTimeMillis()}",
                    supplierName = supplierName,
                    supplierPhone = supplierPhone,
                    itemsJson = itemsJson,
                    totalAmount = totalAmount,
                    paidAmount = paidAmount,
                    status = req.status,
                    notes = notes
                ))
                return RepoResult.Success("Purchase saved offline — will sync when online")
            }

            val response = apiService.addPurchase(req)
            if (response.isSuccessful && response.body()?.success == true) {
                val purchase = response.body()?.purchase
                if (purchase != null) {
                    val itemsJson = moshi.adapter<List<com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseItemEntry>>(
                        com.squareup.moshi.Types.newParameterizedType(List::class.java, com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseItemEntry::class.java)
                    ).toJson(items)
                    purchaseDao.insert(com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseEntity(
                        serverId = purchase.id,
                        supplierName = purchase.supplierName,
                        supplierPhone = purchase.supplierPhone,
                        itemsJson = itemsJson,
                        totalAmount = purchase.totalAmount,
                        paidAmount = purchase.paidAmount,
                        status = purchase.status,
                        notes = purchase.notes,
                        createdAt = try { java.time.Instant.parse(purchase.createdAt).toEpochMilli() } catch (e: Exception) { System.currentTimeMillis() },
                        updatedAt = try { java.time.Instant.parse(purchase.updatedAt).toEpochMilli() } catch (e: Exception) { System.currentTimeMillis() }
                    ))
                }
                com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.LiveSyncManager.requestImmediateSync()
                RepoResult.Success("Purchase recorded successfully")
            } else {
                RepoResult.Error(response.body()?.message ?: "Failed to record purchase")
            }
        } catch (e: Exception) {
            val payload = moshi.adapter(AddPurchaseRequest::class.java).toJson(req)
            pendingDao.insert(PendingOperation(type = "add_purchase", payload = payload))
            val itemsJson = moshi.adapter<List<com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseItemEntry>>(
                com.squareup.moshi.Types.newParameterizedType(List::class.java, com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseItemEntry::class.java)
            ).toJson(items)
            purchaseDao.insert(com.aistudio.sharmakhata.pqmzvk.data.local.PurchaseEntity(
                serverId = "local_${System.currentTimeMillis()}",
                supplierName = supplierName,
                supplierPhone = supplierPhone,
                itemsJson = itemsJson,
                totalAmount = totalAmount,
                paidAmount = paidAmount,
                status = req.status,
                notes = notes
            ))
            RepoResult.Success("Purchase saved offline — will sync later")
        }
    }

    suspend fun deletePurchase(id: Long, serverId: String, context: android.content.Context): RepoResult {
        return try {
            purchaseDao.deleteById(id)
            if (!NetworkUtils.isNetworkAvailable(context)) {
                pendingDao.insert(PendingOperation(type = "delete_purchase", payload = serverId))
                return RepoResult.Success("Purchase deleted offline — will sync when online")
            }

            val response = apiService.deletePurchase(serverId)
            if (response.isSuccessful && response.body()?.success == true) {
                com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.LiveSyncManager.requestImmediateSync()
                RepoResult.Success("Purchase deleted successfully")
            } else {
                RepoResult.Error(response.body()?.message ?: "Failed to delete purchase")
            }
        } catch (e: Exception) {
            pendingDao.insert(PendingOperation(type = "delete_purchase", payload = serverId))
            RepoResult.Success("Purchase deleted offline — will sync later")
        }
    }
}
