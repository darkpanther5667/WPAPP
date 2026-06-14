package com.aistudio.sharmakhata.pqmzvk.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serverId: String = "",
    val supplierName: String,
    val supplierPhone: String = "",
    val itemsJson: String = "[]",
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val status: String = "unpaid",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class PurchaseItemEntry(
    val name: String = "",
    val qty: String = "1",
    val price: String = "",
    val amount: Double = 0.0
)
