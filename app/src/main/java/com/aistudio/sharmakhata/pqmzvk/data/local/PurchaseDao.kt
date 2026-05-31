package com.aistudio.sharmakhata.pqmzvk.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY createdAt DESC")
    fun getAllPurchases(): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE id = :id")
    suspend fun getPurchaseById(id: Long): PurchaseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(purchase: PurchaseEntity): Long

    @Update
    suspend fun update(purchase: PurchaseEntity)

    @Delete
    suspend fun delete(purchase: PurchaseEntity)

    @Query("DELETE FROM purchases WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT SUM(totalAmount) FROM purchases")
    fun getTotalPurchaseAmount(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM purchases")
    fun getPurchaseCount(): Flow<Int>

    @Query("SELECT SUM(totalAmount) FROM purchases WHERE status = 'unpaid' OR status = 'partial'")
    fun getPendingPurchaseAmount(): Flow<Double?>
}
