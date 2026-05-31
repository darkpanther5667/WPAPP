package com.aistudio.sharmakhata.pqmzvk.data.repository

import com.aistudio.sharmakhata.pqmzvk.data.local.ItemDao
import com.aistudio.sharmakhata.pqmzvk.data.local.ItemEntity
import com.aistudio.sharmakhata.pqmzvk.data.remote.ApiClient
import com.aistudio.sharmakhata.pqmzvk.data.remote.StoredItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InventoryRepository @Inject constructor(
    private val itemDao: ItemDao
) {
    fun getAllItems(): Flow<List<ItemEntity>> = itemDao.getAllItems()

    fun searchItems(query: String): Flow<List<ItemEntity>> = itemDao.searchItems(query)

    suspend fun insert(item: ItemEntity) {
        itemDao.insert(item)
    }

    suspend fun update(item: ItemEntity) {
        itemDao.update(item)
    }

    suspend fun deleteById(id: Long) {
        itemDao.deleteById(id)
    }

    suspend fun refreshFromServer() {
        try {
            val response = ApiClient.apiService.getStoredItems()
            response.items.forEach { serverItem ->
                val existing = itemDao.getAllItemsList().find { it.name == serverItem.name }
                if (existing != null) {
                    itemDao.update(existing.copy(price = serverItem.lastPrice, lastPrice = serverItem.lastPrice))
                } else {
                    itemDao.insert(ItemEntity(name = serverItem.name, price = serverItem.lastPrice, lastPrice = serverItem.lastPrice))
                }
            }
        } catch (e: Exception) {
            android.util.Log.d("InventoryRepository", "refreshFromServer error: ${e.message}")
        }
    }

    suspend fun loadStoredItems(): List<StoredItem> {
        return try {
            val response = ApiClient.apiService.getStoredItems()
            response.items
        } catch (e: Exception) {
            emptyList()
        }
    }
}
