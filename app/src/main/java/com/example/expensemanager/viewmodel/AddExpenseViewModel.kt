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
import com.example.expensemanager.repository.ExpenseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class AddExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val expenseDao = db.expenseDao()
    private val repository = ExpenseRepository(expenseDao)
    private val sessionManager = SessionManager(application)

    private val _saveResult = MutableLiveData<Resource<String>>()
    val saveResult: LiveData<Resource<String>> get() = _saveResult

    fun saveExpense(id: Int?, title: String, amountStr: String, date: String, category: String?) {
        if (title.isBlank()) {
            _saveResult.value = Resource.Error("Vui lòng nhập tên chi tiêu!")
            return
        }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _saveResult.value = Resource.Error("Vui lòng nhập số tiền hợp lệ!")
            return
        }
        if (date.isBlank()) {
            _saveResult.value = Resource.Error("Vui lòng chọn ngày!")
            return
        }

        val cleanNote = category?.trim()?.ifEmpty { null }
        val currentUserId = sessionManager.getUserId()

        _saveResult.value = Resource.Loading
        viewModelScope.launch {
            try {
                // Kiểm tra số dư khả dụng trước khi cho phép chi tiêu
                val userItems = expenseDao.getExpensesForUser(currentUserId).first()
                val (incomeItems, expenseItems) = userItems.partition { item ->
                    item.type == "INCOME" || item.title.startsWith("Thu nhập") || (item.note?.contains("Thu nhập") == true)
                }

                val totalIncomeSum = incomeItems.sumOf { it.amount }
                val currentTotalExpense = expenseItems.sumOf { it.amount }

                // Nếu đang chỉnh sửa khoản chi cũ thì cộng trừ chênh lệch khoản chi đó
                val existingExpenseAmount = if (id != null && id != 0) {
                    expenseItems.find { it.id == id }?.amount ?: 0.0
                } else 0.0

                val availableBalance = totalIncomeSum - (currentTotalExpense - existingExpenseAmount)

                if (amount > availableBalance) {
                    val formatter = NumberFormat.getInstance(Locale.GERMANY)
                    val formattedAmount = "${formatter.format(amount.toLong())} đ"
                    val formattedAvailable = "${formatter.format(availableBalance.toLong().coerceAtLeast(0))} đ"

                    _saveResult.value = Resource.Error("Số tiền chi tiêu ($formattedAmount) vượt quá số dư hiện có ($formattedAvailable)!")
                    return@launch
                }

                if (id == null || id == 0) {
                    // Thêm mới
                    val newExpense = ExpenseEntity(
                        title = title,
                        amount = amount,
                        date = date,
                        note = cleanNote,
                        type = "EXPENSE",
                        userId = currentUserId
                    )
                    repository.addExpense(newExpense)
                    _saveResult.value = Resource.Success("Thêm khoản chi tiêu thành công!")
                } else {
                    // Cập nhật (Sửa)
                    val updatedExpense = ExpenseEntity(
                        id = id,
                        title = title,
                        amount = amount,
                        date = date,
                        note = cleanNote,
                        type = "EXPENSE",
                        userId = currentUserId
                    )
                    repository.updateExpense(updatedExpense)
                    _saveResult.value = Resource.Success("Cập nhật khoản chi tiêu thành công!")
                }
            } catch (e: Exception) {
                _saveResult.value = Resource.Error("Lỗi: ${e.localizedMessage}")
            }
        }
    }

    fun deleteExpense(id: Int, title: String, amount: Double, date: String, category: String?) {
        val currentUserId = sessionManager.getUserId()
        _saveResult.value = Resource.Loading
        viewModelScope.launch {
            try {
                val expenseToDelete = ExpenseEntity(
                    id = id,
                    title = title,
                    amount = amount,
                    date = date,
                    note = category?.trim()?.ifEmpty { null },
                    type = "EXPENSE",
                    userId = currentUserId
                )
                repository.deleteExpense(expenseToDelete)
                _saveResult.value = Resource.Success("Đã xóa khoản chi tiêu!")
            } catch (e: Exception) {
                _saveResult.value = Resource.Error("Lỗi khi xóa: ${e.localizedMessage}")
            }
        }
    }
}
