package com.example.data.remote

import com.example.data.model.DailyReport
import com.example.data.model.FullDatabase
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("api/db")
    suspend fun getFullDatabase(): FullDatabase

    @GET("api/report")
    suspend fun getDailyReport(): DailyReport

    @POST("api/db")
    suspend fun updateDatabase(@Body db: FullDatabase): retrofit2.Response<Unit>

    @POST("api/customer/add")
    suspend fun addCustomer(@Body request: AddCustomerRequest): retrofit2.Response<AddCustomerResponse>

    @POST("api/payment/add")
    suspend fun addPayment(@Body request: AddPaymentRequest): retrofit2.Response<Unit>

    @POST("api/bill/create")
    suspend fun createBill(@Body request: CreateBillRequest): retrofit2.Response<Unit>
}

data class AddCustomerRequest(
    val name: String,
    val phone: String
)

data class AddCustomerResponse(
    val success: Boolean,
    val customer: com.example.data.model.Customer?,
    val message: String?
)

data class AddPaymentRequest(
    val customerId: String,
    val amount: Double,
    val note: String?
)

data class CreateBillRequest(
    val customerId: String,
    val amount: Double,
    val items: List<BillItemRequest>?
)

data class BillItemRequest(
    val name: String,
    val price: Double,
    val qty: Int
)
