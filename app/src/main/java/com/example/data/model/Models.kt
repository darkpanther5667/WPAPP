package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FullDatabase(
    val shop: Shop?,
    val customers: List<Customer> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val bills: List<Bill> = emptyList(),
    val staff: List<Staff> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Shop(
    val name: String? = null,
    val owner: String? = null,
    val address: String? = null
)

@JsonClass(generateAdapter = true)
data class Customer(
    val id: String,
    val name: String,
    val phone: String?,
    @Json(name = "created_at") val createdAt: String?
)

@JsonClass(generateAdapter = true)
data class Transaction(
    val id: String,
    @Json(name = "customer_id") val customerId: String,
    val type: String, // 'payment' or 'credit'
    val amount: Double,
    val note: String?,
    @Json(name = "staff_phone") val staffPhone: String?,
    val timestamp: String
)

@JsonClass(generateAdapter = true)
data class Bill(
    val id: String,
    @Json(name = "customer_id") val customerId: String,
    val items: List<BillItem> = emptyList(),
    val total: Double,
    val status: String, // 'unpaid' or 'paid'
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "paid_at") val paidAt: String?
)

@JsonClass(generateAdapter = true)
data class BillItem(
    val name: String,
    val qty: Int,
    val price: Double
)

@JsonClass(generateAdapter = true)
data class Staff(
    val id: String,
    val name: String,
    val phone: String
)

@JsonClass(generateAdapter = true)
data class DailyReport(
    val date: String,
    val billsTotal: Double,
    val paymentTotal: Double,
    val billsCount: Int,
    val outstanding: List<OutstandingCustomer> = emptyList()
)

@JsonClass(generateAdapter = true)
data class OutstandingCustomer(
    val name: String,
    val phone: String?,
    val balance: Double
)
