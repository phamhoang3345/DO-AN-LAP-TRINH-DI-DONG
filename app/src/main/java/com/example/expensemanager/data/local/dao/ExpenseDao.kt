package com.example.expensemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensemanager.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY id DESC")
    fun getExpensesForUser(userId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY id DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :expenseId LIMIT 1")
    fun getExpenseById(expenseId: Int): Flow<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    // --- Phần Danh sách ---
    @Query("SELECT * FROM expenses WHERE type = :type ORDER BY date DESC, id DESC")
    fun getExpensesByType(type: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date = :date ORDER BY id DESC")
    fun getExpensesByDate(date: String): Flow<List<ExpenseEntity>>

    @Query(
        """
        SELECT * FROM expenses
        WHERE title LIKE '%' || :query || '%'
           OR IFNULL(note, '') LIKE '%' || :query || '%'
        ORDER BY date DESC, id DESC
        """
    )
    fun searchExpenses(query: String): Flow<List<ExpenseEntity>>

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: Int)

    // --- Phần Tổng quan ---
    @Query("SELECT IFNULL(SUM(amount), 0.0) FROM expenses WHERE type = 'EXPENSE' OR type IS NULL OR type != 'INCOME'")
    fun getTotalExpense(): Flow<Double>

    @Query("SELECT IFNULL(SUM(amount), 0.0) FROM expenses WHERE type = 'INCOME'")
    fun getTotalIncomeFromExpenses(): Flow<Double>

    @Query("SELECT IFNULL(SUM(amount), 0.0) FROM expenses WHERE date LIKE :datePattern AND (type = 'EXPENSE' OR type IS NULL OR type != 'INCOME')")
    fun getTotalExpenseByDatePattern(datePattern: String): Flow<Double>

    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC LIMIT :limit")
    fun getRecentExpenses(limit: Int = 5): Flow<List<ExpenseEntity>>
}