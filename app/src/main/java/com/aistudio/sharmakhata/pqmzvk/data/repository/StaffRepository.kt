package com.aistudio.sharmakhata.pqmzvk.data.repository

import android.content.Context
import com.aistudio.sharmakhata.pqmzvk.data.model.Staff
import com.aistudio.sharmakhata.pqmzvk.data.remote.AddStaffRequest
import com.aistudio.sharmakhata.pqmzvk.data.remote.ApiService
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.RepoResult
import com.aistudio.sharmakhata.pqmzvk.util.NetworkUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StaffRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun addStaff(name: String, phone: String, role: String, context: Context): RepoResult {
        return try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                return RepoResult.Error("No internet connection. Please try again when online.")
            }
            val response = apiService.addStaff(AddStaffRequest(name, phone, role))
            if (response.isSuccessful && response.body()?.success == true) {
                com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.LiveSyncManager.requestImmediateSync()
                RepoResult.Success("Staff member added successfully")
            } else {
                val msg = response.body()?.message ?: "Failed to add staff"
                RepoResult.Error(msg)
            }
        } catch (e: Exception) {
            RepoResult.Error(e.message ?: "Network error")
        }
     }

    suspend fun removeStaff(id: String, context: Context): RepoResult {
        return try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                return RepoResult.Error("No internet connection. Please try again when online.")
            }
            val response = apiService.removeStaff(id)
            if (response.isSuccessful && response.body()?.success == true) {
                com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.LiveSyncManager.requestImmediateSync()
                RepoResult.Success("Staff member removed")
            } else {
                val msg = response.body()?.message ?: "Failed to remove staff"
                RepoResult.Error(msg)
            }
        } catch (e: Exception) {
            RepoResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun getStaffList(): List<Staff> {
        return try {
            val response = apiService.getFullDatabase()
            if (response.isSuccessful) {
                response.body()?.staff ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
