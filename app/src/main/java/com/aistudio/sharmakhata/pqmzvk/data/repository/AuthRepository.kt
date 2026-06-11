package com.aistudio.sharmakhata.pqmzvk.data.repository

import com.aistudio.sharmakhata.pqmzvk.data.remote.ApiService
import com.aistudio.sharmakhata.pqmzvk.data.remote.LoginWithPasswordRequest
import com.aistudio.sharmakhata.pqmzvk.data.remote.RegisterStoreRequest
import com.aistudio.sharmakhata.pqmzvk.data.remote.RequestLoginCodeRequest
import com.aistudio.sharmakhata.pqmzvk.data.remote.UpdateStoreProfileRequest
import com.aistudio.sharmakhata.pqmzvk.data.remote.VerifyLoginCodeRequest
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data class Success(val token: String? = null, val storeId: String? = null, val message: String? = null) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun requestLoginCode(storeId: String, phone: String): AuthResult {
        return try {
            val response = apiService.requestLoginCode(RequestLoginCodeRequest(storeId, phone))
            if (response.isSuccessful && response.body()?.success == true) {
                AuthResult.Success(message = "Code sent on WhatsApp")
            } else {
                AuthResult.Error(response.body()?.message ?: "Failed to send OTP")
            }
        } catch (e: Exception) {
            AuthResult.Error(mapNetworkError(e))
        }
    }

    suspend fun verifyLoginCode(storeId: String, phone: String, code: String): AuthResult {
        return try {
            val response = apiService.verifyLoginCode(VerifyLoginCodeRequest(storeId, phone, code))
            val body = response.body()
            val token = body?.token
            if (response.isSuccessful && !token.isNullOrBlank()) {
                val storeMap = body?.store as? Map<*, *>
                val storeIdFromServer = storeMap?.get("id") as? String
                AuthResult.Success(token = token, storeId = storeIdFromServer)
            } else {
                AuthResult.Error(body?.message ?: "Invalid or expired OTP")
            }
        } catch (e: Exception) {
            AuthResult.Error(mapNetworkError(e))
        }
    }

    suspend fun loginWithPassword(phone: String, password: String): AuthResult {
        return try {
            val response = apiService.loginWithPassword(LoginWithPasswordRequest(phone, password))
            val body = response.body()
            if (response.isSuccessful && body?.success == true && !body.token.isNullOrBlank()) {
                val storeMap = body.store as? Map<*, *>
                val storeIdFromServer = storeMap?.get("id") as? String
                AuthResult.Success(token = body.token, storeId = storeIdFromServer)
            } else {
                AuthResult.Error(body?.message ?: "Login failed. Check your phone and password.")
            }
        } catch (e: Exception) {
            AuthResult.Error(mapNetworkError(e))
        }
    }

    suspend fun registerStore(
        storeName: String,
        ownerName: String,
        phone: String,
        email: String,
        address: String?,
        gstin: String?,
        password: String?
    ): AuthResult {
        return try {
            val response = apiService.registerStore(
                RegisterStoreRequest(
                    store_name = storeName,
                    owner_name = ownerName,
                    phone = phone,
                    email = email,
                    address = address,
                    gstin = gstin,
                    password = password,
                )
            )
            val body = response.body()
            val storeId = body?.store_id
            val status = body?.status
            if (!storeId.isNullOrBlank()) {
                val msg = when (status) {
                    "exists" -> "Store already exists — you can login now"
                    else -> body?.message ?: "Store registered"
                }
                AuthResult.Success(storeId = storeId, message = msg)
            } else {
                AuthResult.Error(body?.message ?: "Failed to register store")
            }
        } catch (e: Exception) {
            AuthResult.Error("Failed to register store. Please try again.")
        }
    }

    suspend fun updateStoreProfile(
        storeName: String,
        ownerName: String,
        address: String?,
        upiId: String?,
        gstin: String?,
        invoiceTemplate: String?
    ): AuthResult {
        return try {
            val response = apiService.updateStoreProfile(
                UpdateStoreProfileRequest(
                    store_name = storeName,
                    owner_name = ownerName,
                    address = address,
                    upi_id = upiId,
                    gstin = gstin,
                    invoice_template = invoiceTemplate
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                AuthResult.Success(message = "Store profile updated successfully")
            } else {
                AuthResult.Error(response.body()?.message ?: "Failed to update profile")
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error. Failed to update profile.")
        }
    }

    private fun mapNetworkError(e: Exception): String {
        return when (e) {
            is java.net.SocketTimeoutException -> "Connection timed out. Please check your internet connection and try again."
            is java.net.UnknownHostException -> "No internet connection. Please check your network and try again."
            is java.net.ConnectException -> "Unable to connect to server. Please try again."
            else -> "Network error. Please check your connection."
        }
    }
}
