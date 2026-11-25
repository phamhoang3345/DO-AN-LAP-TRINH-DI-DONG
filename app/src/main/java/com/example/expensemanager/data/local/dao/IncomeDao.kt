package com.example.expensemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.expensemanager.data.local.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {

    @Query("SELECT * FROM incomes ORDER BY id DESC")
    fun getAllIncomes(): Flow<List<IncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity): Long

    @Query("DELETE FROM incomes")
    suspend fun deleteAllIncomes()

    // --- Phần Tổng quan & Danh sách ---
    @Query("SELECT IFNULL(SUM(amount), 0.0) FROM incomes")
    fun getTotalIncome(): Flow<Double>

    @Query("SELECT IFNULL(SUM(amount), 0.0) FROM incomes WHERE date LIKE :datePattern")
    fun getTotalIncomeByDatePattern(datePattern: String): Flow<Double>

    @Query(
        """
        SELECT * FROM incomes
        WHERE source LIKE '%' || :query || '%'
           OR IFNULL(note, '') LIKE '%' || :query || '%'
        ORDER BY date DESC, id DESC
        """
    )
    fun searchIncomes(query: String): Flow<List<IncomeEntity>>
}