package com.aistudio.sharmakhata.pqmzvk.data.repository

import android.content.Context
import com.aistudio.sharmakhata.pqmzvk.data.local.PendingDao
import com.aistudio.sharmakhata.pqmzvk.data.local.PendingOperation
import com.aistudio.sharmakhata.pqmzvk.data.remote.AddPaymentRequest
import com.aistudio.sharmakhata.pqmzvk.data.remote.ApiService
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.RepoResult
import com.aistudio.sharmakhata.pqmzvk.util.NetworkUtils
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val pendingDao: PendingDao,
    private val apiService: ApiService,
    private val moshi: Moshi
) {

    suspend fun addPayment(
        context: Context,
        customerId: String,
        amount: Double,
        note: String?,
        paymentMode: String = "cash",
        type: String = "payment"
    ): RepoResult {
        return try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                val payload = moshi.adapter(AddPaymentRequest::class.java).toJson(
                    AddPaymentRequest(customerId, amount, note, paymentMode, type)
                )
                pendingDao.insert(PendingOperation(type = "add_payment", payload = payload))
                return RepoResult.Success("Payment saved - will sync when online")
            }

            val response = apiService.addPayment(
                AddPaymentRequest(customerId, amount, note, paymentMode, type)
            )
            if (response.isSuccessful) {
                com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.LiveSyncManager.requestImmediateSync()
                RepoResult.Success("Payment recorded successfully")
            } else {
                RepoResult.Error("Failed to record payment")
            }
        } catch (e: Exception) {
            val payload = moshi.adapter(AddPaymentRequest::class.java).toJson(
                AddPaymentRequest(customerId, amount, note, paymentMode, type)
            )
            pendingDao.insert(PendingOperation(type = "add_payment", payload = payload))
            RepoResult.Success("Payment saved offline - will sync later")
        }
    }
}
