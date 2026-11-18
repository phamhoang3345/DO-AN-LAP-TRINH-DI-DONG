package com.example.expensemanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.common.Resource
import com.example.expensemanager.data.local.AppDatabase
import com.example.expensemanager.data.local.SessionManager
import com.example.expensemanager.data.local.entity.ExpenseEntity
import com.example.expensemanager.data.local.entity.IncomeEntity
import kotlinx.coroutines.launch

class AddIncomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val incomeDao = db.incomeDao()
    private val expenseDao = db.expenseDao()
    private val sessionManager = SessionManager(application)

    private val _saveResult = MutableLiveData<Resource<String>>()
    val saveResult: LiveData<Resource<String>> get() = _saveResult

    fun saveIncome(amountStr: String, source: String, date: String, note: String?) {
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _saveResult.value = Resource.Error("Vui lòng nhập số tiền hợp lệ!")
            return
        }
        if (source.isBlank()) {
            _saveResult.value = Resource.Error("Vui lòng nhập nguồn thu!")
            return
        }
        if (date.isBlank()) {
            _saveResult.value = Resource.Error("Vui lòng chọn ngày nhận!")
            return
        }

        _saveResult.value = Resource.Loading
        viewModelScope.launch {
            try {
                // 1. Lưu khoản thu vào Room DB Bảng Incomes
                val newIncome = IncomeEntity(
                    amount = amount,
                    source = source.trim(),
                    date = date.trim(),
                    note = note?.trim()
                )
                incomeDao.insertIncome(newIncome)

                // 2. Tự động lưu vào Bảng Expenses để hiển thị bên tab Chi tiêu (Không còn tiền tố "Thu nhập:" ở tiêu đề)
                val incomeExpenseEntity = ExpenseEntity(
                    title = source.trim(),
                    amount = amount,
                    date = date.trim(),
                    note = if (!note.isNullOrBlank()) "Thu nhập (${note.trim()})" else "Thu nhập"
                )
                expenseDao.insertExpense(incomeExpenseEntity)

                // 3. Cập nhật tổng thu và số dư vào SessionManager
                val currentTotalIncome = sessionManager.getTotalIncome()
                val currentBalance = sessionManager.getBalance()
                sessionManager.saveTotalIncome(currentTotalIncome + amount)
                sessionManager.saveBalance(currentBalance + amount)

                _saveResult.value = Resource.Success("Thêm khoản thu thành công!")
            } catch (e: Exception) {
                _saveResult.value = Resource.Error("Lỗi: ${e.localizedMessage}")
            }
        }
    }
}
