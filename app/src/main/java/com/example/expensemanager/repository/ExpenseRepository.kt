package com.example.expensemanager.repository

import android.util.Log
import com.example.expensemanager.api.ApiClient
import com.example.expensemanager.common.Resource
import com.example.expensemanager.data.local.dao.ExpenseDao
import com.example.expensemanager.data.local.dao.IncomeDao
import com.example.expensemanager.data.local.entity.ExpenseEntity
import com.example.expensemanager.model.Expense
import com.example.expensemanager.model.OverviewSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val incomeDao: IncomeDao? = null
) {

    private val numberFormatter = NumberFormat.getInstance(Locale.GERMANY)

    // Lấy dữ liệu dạng Flow từ Local DB (Room) cho đúng User ID
    fun getExpensesFlow(userId: String): Flow<Resource<List<Expense>>> {
        return expenseDao.getExpensesForUser(userId).map { entities ->
            val expenses = entities.map { entity ->
                val formattedAmount = try {
                    numberFormatter.format(entity.amount.toLong())
                } catch (e: Exception) {
                    entity.amount.toString()
                }

                Expense(
                    id = entity.id.toString(),
                    title = entity.title,
                    amount = "$formattedAmount đ",
                    date = entity.date,
                    category = entity.note?.ifBlank { null },
                    type = entity.type
                )
            }
            Resource.Success(expenses)
        }
    }

    // Thêm mới chi tiêu vào Room DB
    suspend fun addExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.insertExpense(expense)
    }

    // Cập nhật khoản chi tiêu trong Room DB
    suspend fun updateExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.updateExpense(expense)
    }

    // Xóa khoản chi tiêu trong Room DB
    suspend fun deleteExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpense(expense)
    }

    // Hàm đồng bộ dữ liệu từ API về Local DB cho đúng User ID
    suspend fun syncExpensesFromApi(userId: String) = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.apiService.getExpenses()
            if (response.isSuccessful) {
                val apiExpenses = response.body()
                if (apiExpenses != null) {
                    for (expense in apiExpenses) {
                        val entity = ExpenseEntity(
                            title = expense.title,
                            amount = expense.amount.toDoubleOrNull() ?: 0.0,
                            date = expense.date,
                            note = expense.category,
                            type = expense.type,
                            userId = userId
                        )
                        expenseDao.insertExpense(entity)
                    }
                    Log.d("ExpenseRepository", "Sync API to Local DB successful")
                }
            } else {
                Log.e("ExpenseRepository", "API Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("ExpenseRepository", "Offline mode / Network error: ${e.localizedMessage}")
        }
    }
}
