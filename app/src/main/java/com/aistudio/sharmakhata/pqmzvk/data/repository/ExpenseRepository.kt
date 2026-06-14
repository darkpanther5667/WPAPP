package com.aistudio.sharmakhata.pqmzvk.data.repository

import com.aistudio.sharmakhata.pqmzvk.data.local.ExpenseDao
import com.aistudio.sharmakhata.pqmzvk.data.local.ExpenseEntity
import com.aistudio.sharmakhata.pqmzvk.data.local.PendingDao
import com.aistudio.sharmakhata.pqmzvk.data.local.PendingOperation
import com.aistudio.sharmakhata.pqmzvk.data.remote.AddExpenseRequest
import com.aistudio.sharmakhata.pqmzvk.data.remote.ApiService
import com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.RepoResult
import com.aistudio.sharmakhata.pqmzvk.util.NetworkUtils
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val pendingDao: PendingDao,
    private val apiService: ApiService,
    private val moshi: Moshi
) {
    fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    fun getTotalExpensesBetween(start: Long, end: Long): Flow<Double?> =
        expenseDao.getTotalExpensesBetween(start, end)

    suspend fun addExpense(title: String, amount: Double, category: String, note: String?, context: android.content.Context): RepoResult {
        return try {
            val req = AddExpenseRequest(title, amount, category, note)
            if (!NetworkUtils.isNetworkAvailable(context)) {
                val payload = moshi.adapter(AddExpenseRequest::class.java).toJson(req)
                pendingDao.insert(PendingOperation(type = "add_expense", payload = payload))
                expenseDao.insert(ExpenseEntity(serverId = "local_${System.currentTimeMillis()}", title = title, amount = amount, category = category, note = note))
                return RepoResult.Success("Expense saved offline — will sync when online")
            }

            val response = apiService.addExpense(req)
            if (response.isSuccessful && response.body()?.success == true) {
                val expense = response.body()?.expense
                if (expense != null) {
                    expenseDao.insert(ExpenseEntity(
                        serverId = expense.id,
                        title = expense.title,
                        amount = expense.amount,
                        category = expense.category,
                        note = expense.note,
                        createdAt = try { java.time.Instant.parse(expense.createdAt).toEpochMilli() } catch (e: Exception) { System.currentTimeMillis() }
                    ))
                }
                com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.LiveSyncManager.requestImmediateSync()
                RepoResult.Success("Expense added successfully")
            } else {
                RepoResult.Error(response.body()?.message ?: "Failed to add expense")
            }
        } catch (e: Exception) {
            val req = AddExpenseRequest(title, amount, category, note)
            val payload = moshi.adapter(AddExpenseRequest::class.java).toJson(req)
            pendingDao.insert(PendingOperation(type = "add_expense", payload = payload))
            expenseDao.insert(ExpenseEntity(serverId = "local_${System.currentTimeMillis()}", title = title, amount = amount, category = category, note = note))
            RepoResult.Success("Expense saved offline — will sync later")
        }
    }

    suspend fun deleteExpense(id: Long, serverId: String, context: android.content.Context): RepoResult {
        return try {
            expenseDao.deleteById(id)
            if (!NetworkUtils.isNetworkAvailable(context)) {
                pendingDao.insert(PendingOperation(type = "delete_expense", payload = serverId))
                return RepoResult.Success("Expense deleted offline — will sync when online")
            }

            val response = apiService.deleteExpense(serverId)
            if (response.isSuccessful && response.body()?.success == true) {
                com.aistudio.sharmakhata.pqmzvk.ui.viewmodel.LiveSyncManager.requestImmediateSync()
                RepoResult.Success("Expense deleted successfully")
            } else {
                RepoResult.Error(response.body()?.message ?: "Failed to delete expense")
            }
        } catch (e: Exception) {
            pendingDao.insert(PendingOperation(type = "delete_expense", payload = serverId))
            RepoResult.Success("Expense deleted offline — will sync later")
        }
    }
}
