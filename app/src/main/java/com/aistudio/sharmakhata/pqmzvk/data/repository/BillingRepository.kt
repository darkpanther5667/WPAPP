package com.aistudio.sharmakhata.pqmzvk.data.repository

import android.content.Context
import com.aistudio.sharmakhata.pqmzvk.data.local.PendingDao
import com.aistudio.sharmakhata.pqmzvk.data.local.PendingOperation
import com.aistudio.sharmakhata.pqmzvk.data.remote.*
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.RepoResult
import com.aistudio.sharmakhata.pqmzvk.util.Constants
import com.aistudio.sharmakhata.pqmzvk.util.NetworkUtils
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepository @Inject constructor(
    private val pendingDao: PendingDao,
    private val apiService: ApiService,
    private val moshi: Moshi
) {

    suspend fun createBill(
        context: Context,
        customerId: String,
        amount: Double,
        items: List<BillItemRequest>?,
        gstType: String = "cgst_sgst",
        gstRate: Int = 0,
        taxableAmount: Double = 0.0,
        totalCgst: Double = 0.0,
        totalSgst: Double = 0.0,
        totalIgst: Double = 0.0,
        grandTotal: Double = 0.0,
        invoiceNumber: String? = null,
        discount: Double = 0.0
    ): Pair<RepoResult, String?> {
        return try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                val payload = moshi.adapter(CreateBillRequest::class.java).toJson(
                    CreateBillRequest(customerId = customerId, amount = amount, items = items, invoiceNumber = invoiceNumber, discount = discount, gstType = gstType, gstRate = gstRate, taxableAmount = taxableAmount, totalCgst = totalCgst, totalSgst = totalSgst, totalIgst = totalIgst, grandTotal = grandTotal)
                )
                pendingDao.insert(PendingOperation(type = "create_bill", payload = payload))
                deductStockLocally(context, items)
                return Pair(RepoResult.Success("Bill saved - will sync when online"), null)
            }

            val response = apiService.createBill(
                CreateBillRequest(customerId = customerId, amount = amount, items = items, invoiceNumber = invoiceNumber, discount = discount, gstType = gstType, gstRate = gstRate, taxableAmount = taxableAmount, totalCgst = totalCgst, totalSgst = totalSgst, totalIgst = totalIgst, grandTotal = grandTotal)
            )
            val billId = response.body()?.billId
            if (response.isSuccessful && !billId.isNullOrBlank()) {
                deductStockLocally(context, items)
                com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.LiveSyncManager.requestImmediateSync()
                Pair(RepoResult.Success("Bill created (ID: $billId)"), billId)
            } else {
                Pair(RepoResult.Error("Failed to create bill"), null)
            }
        } catch (e: Exception) {
            val payload = moshi.adapter(CreateBillRequest::class.java).toJson(
                CreateBillRequest(customerId = customerId, amount = amount, items = items, invoiceNumber = invoiceNumber, discount = discount, gstType = gstType, gstRate = gstRate, taxableAmount = taxableAmount, totalCgst = totalCgst, totalSgst = totalSgst, totalIgst = totalIgst, grandTotal = grandTotal)
            )
            pendingDao.insert(PendingOperation(type = "create_bill", payload = payload))
            deductStockLocally(context, items)
            Pair(RepoResult.Success("Bill saved offline - will sync later"), null)
        }
    }

    suspend fun quickBill(
        context: Context,
        customerName: String?,
        customerPhone: String?,
        total: Double,
        items: List<BillItemRequest>?,
        gstType: String = "cgst_sgst",
        gstRate: Int = 0,
        taxableAmount: Double = 0.0,
        totalCgst: Double = 0.0,
        totalSgst: Double = 0.0,
        totalIgst: Double = 0.0,
        grandTotal: Double = 0.0,
        invoiceNumber: String? = null,
        discount: Double = 0.0
    ): Pair<RepoResult, String?> {
        return try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                val payload = moshi.adapter(CreateBillRequest::class.java).toJson(
                    CreateBillRequest(customerId = Constants.WALK_IN_CUSTOMER_ID, amount = grandTotal, items = items, invoiceNumber = invoiceNumber, discount = discount, gstType = gstType, gstRate = gstRate, taxableAmount = taxableAmount, totalCgst = totalCgst, totalSgst = totalSgst, totalIgst = totalIgst, grandTotal = grandTotal)
                )
                pendingDao.insert(PendingOperation(type = "create_bill", payload = payload))
                deductStockLocally(context, items)
                return Pair(RepoResult.Success("Bill saved - will sync when online"), null)
            }

            val response = apiService.createBill(
                CreateBillRequest(customerId = Constants.WALK_IN_CUSTOMER_ID, amount = grandTotal, items = items, invoiceNumber = invoiceNumber, discount = discount, gstType = gstType, gstRate = gstRate, taxableAmount = taxableAmount, totalCgst = totalCgst, totalSgst = totalSgst, totalIgst = totalIgst, grandTotal = grandTotal)
            )
            val billId = response.body()?.billId
            if (response.isSuccessful && !billId.isNullOrBlank()) {
                deductStockLocally(context, items)
                com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.LiveSyncManager.requestImmediateSync()
                Pair(RepoResult.Success("Bill created (ID: $billId)"), billId)
            } else {
                Pair(RepoResult.Error("Failed to create bill"), null)
            }
        } catch (e: Exception) {
            val payload = moshi.adapter(CreateBillRequest::class.java).toJson(
                CreateBillRequest(customerId = Constants.WALK_IN_CUSTOMER_ID, amount = grandTotal, items = items, invoiceNumber = invoiceNumber, discount = discount, gstType = gstType, gstRate = gstRate, taxableAmount = taxableAmount, totalCgst = totalCgst, totalSgst = totalSgst, totalIgst = totalIgst, grandTotal = grandTotal)
            )
            pendingDao.insert(PendingOperation(type = "create_bill", payload = payload))
            deductStockLocally(context, items)
            Pair(RepoResult.Success("Bill saved offline - will sync later"), null)
        }
    }

    suspend fun markBillPaid(context: Context, billId: String): RepoResult {
        return try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                val payload = moshi.adapter(MarkBillPaidRequest::class.java).toJson(MarkBillPaidRequest(billId))
                pendingDao.insert(PendingOperation(type = "mark_paid", payload = payload))
                return RepoResult.Success("Saved offline — will sync when online")
            }

            val response = apiService.markBillPaid(MarkBillPaidRequest(billId))
            if (response.isSuccessful && response.body()?.success == true) {
                com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.LiveSyncManager.requestImmediateSync()
                RepoResult.Success("Bill marked as paid")
            } else {
                RepoResult.Error("Failed to mark bill paid")
            }
        } catch (e: Exception) {
            val payload = moshi.adapter(MarkBillPaidRequest::class.java).toJson(MarkBillPaidRequest(billId))
            pendingDao.insert(PendingOperation(type = "mark_paid", payload = payload))
            RepoResult.Success("Saved offline — will sync later")
        }
    }

    suspend fun sendInvoiceOnWhatsApp(context: Context, billId: String): RepoResult {
        return try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                return RepoResult.Error("No internet connection")
            }
            val response = apiService.sendInvoice(SendInvoiceRequest(billId))
            if (response.isSuccessful && response.body()?.success == true) {
                RepoResult.Success("Invoice sent on WhatsApp")
            } else {
                RepoResult.Error("Failed to send invoice")
            }
        } catch (e: Exception) {
            RepoResult.Error("Failed to send invoice")
        }
    }

    suspend fun sendStatementOnWhatsApp(context: Context, customerId: String): RepoResult {
        return try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                return RepoResult.Error("No internet connection")
            }
            val response = apiService.sendStatement(SendStatementRequest(customerId))
            if (response.isSuccessful && response.body()?.success == true) {
                RepoResult.Success("Statement sent on WhatsApp")
            } else {
                RepoResult.Error("Failed to send statement")
            }
        } catch (e: Exception) {
            RepoResult.Error("Failed to send statement")
        }
    }

    suspend fun sendReminderOnWhatsApp(context: Context, customerId: String, message: String? = null): RepoResult {
        return try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                return RepoResult.Error("No internet connection")
            }
            val response = apiService.sendReminder(SendReminderRequest(customerId, message))
            if (response.isSuccessful && response.body()?.success == true) {
                RepoResult.Success("Reminder sent on WhatsApp")
            } else {
                RepoResult.Error("Failed to send reminder")
            }
        } catch (e: Exception) {
            RepoResult.Error("Failed to send reminder")
        }
    }

    suspend fun loadStoredItems(): List<StoredItem> {
        return try {
            val response = apiService.getStoredItems()
            response.items
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteBill(context: Context, billId: String): RepoResult {
        return try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                return RepoResult.Error("No internet connection — delete requires online access")
            }
            val response = apiService.deleteBill(billId)
            if (response.isSuccessful && response.body()?.success == true) {
                com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.LiveSyncManager.requestImmediateSync()
                RepoResult.Success("Bill deleted")
            } else {
                RepoResult.Error(response.body()?.message ?: "Failed to delete bill")
            }
        } catch (e: Exception) {
            RepoResult.Error("Failed to delete bill: ${e.message}")
        }
    }

    suspend fun deleteTransaction(context: Context, transactionId: String): RepoResult {
        return try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                return RepoResult.Error("No internet connection — delete requires online access")
            }
            val response = apiService.deleteTransaction(transactionId)
            if (response.isSuccessful && response.body()?.success == true) {
                com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.LiveSyncManager.requestImmediateSync()
                RepoResult.Success("Transaction deleted")
            } else {
                RepoResult.Error(response.body()?.message ?: "Failed to delete transaction")
            }
        } catch (e: Exception) {
            RepoResult.Error("Failed to delete transaction: ${e.message}")
        }
    }

    private suspend fun deductStockLocally(context: Context, items: List<BillItemRequest>?) {
        try {
            val db = com.aistudio.sharmakhata.pqmzvk.data.local.AppDatabase.get(context)
            val itemDao = db.itemDao()
            items?.forEach { billItem ->
                val matchingItem = itemDao.getAllItemsList().find { it.name.trim().equals(billItem.name.trim(), ignoreCase = true) }
                if (matchingItem != null) {
                    itemDao.reduceStock(matchingItem.id, billItem.qty)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("BillingRepository", "Stock deduction failed: ${e.message}")
        }
    }
}
