package com.aistudio.sharmakhata.pqmzvk.data.repository

import android.content.Context
import com.aistudio.sharmakhata.pqmzvk.data.local.PendingDao
import com.aistudio.sharmakhata.pqmzvk.data.local.PendingOperation
import com.aistudio.sharmakhata.pqmzvk.data.remote.AddCustomerRequest
import com.aistudio.sharmakhata.pqmzvk.data.remote.ApiService
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.RepoResult
import com.aistudio.sharmakhata.pqmzvk.util.NetworkUtils
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val pendingDao: PendingDao,
    private val apiService: ApiService,
    private val moshi: Moshi
) {

    suspend fun addCustomer(name: String, phone: String, context: Context): RepoResult {
        return try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                val payload = moshi.adapter(AddCustomerRequest::class.java)
                    .toJson(AddCustomerRequest(name, phone))
                pendingDao.insert(PendingOperation(type = "add_customer", payload = payload))
                return RepoResult.Success("Customer saved — will sync when online")
            }

            val response = apiService.addCustomer(AddCustomerRequest(name, phone))
            if (response.isSuccessful && response.body()?.success == true) {
                RepoResult.Success("Customer added successfully")
            } else {
                val msg = response.body()?.message ?: "Failed to add customer"
                if (response.code() == 409) {
                    RepoResult.Success("Customer already exists")
                } else {
                    RepoResult.Error(msg)
                }
            }
        } catch (e: Exception) {
            val payload = moshi.adapter(AddCustomerRequest::class.java)
                .toJson(AddCustomerRequest(name, phone))
            pendingDao.insert(PendingOperation(type = "add_customer", payload = payload))
            RepoResult.Success("Customer saved offline — will sync later")
        }
    }
}
