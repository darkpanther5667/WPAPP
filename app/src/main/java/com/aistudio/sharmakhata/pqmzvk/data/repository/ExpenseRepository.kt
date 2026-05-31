package com.aistudio.sharmakhata.pqmzvk.data.repository

import com.aistudio.sharmakhata.pqmzvk.data.local.ExpenseDao
import com.aistudio.sharmakhata.pqmzvk.data.local.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    fun getTotalExpensesBetween(start: Long, end: Long): Flow<Double?> =
        expenseDao.getTotalExpensesBetween(start, end)

    suspend fun insert(expense: ExpenseEntity) {
        expenseDao.insert(expense)
    }

    suspend fun deleteById(id: Long) {
        expenseDao.deleteById(id)
    }
}
