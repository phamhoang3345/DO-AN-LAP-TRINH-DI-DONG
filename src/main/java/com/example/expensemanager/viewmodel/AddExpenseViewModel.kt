package com.example.expensemanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.common.Resource
import com.example.expensemanager.data.local.AppDatabase
import com.example.expensemanager.data.local.entity.ExpenseEntity
import com.example.expensemanager.repository.ExpenseRepository
import kotlinx.coroutines.launch

class AddExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository

    private val _saveResult = MutableLiveData<Resource<String>>()
    val saveResult: LiveData<Resource<String>> get() = _saveResult

    init {
        val expenseDao = AppDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(expenseDao)
    }

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

        _saveResult.value = Resource.Loading
        viewModelScope.launch {
            try {
                if (id == null || id == 0) {
                    // Thêm mới
                    val newExpense = ExpenseEntity(
                        title = title,
                        amount = amount,
                        date = date,
                        note = category
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
                        note = category
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
        _saveResult.value = Resource.Loading
        viewModelScope.launch {
            try {
                val expenseToDelete = ExpenseEntity(
                    id = id,
                    title = title,
                    amount = amount,
                    date = date,
                    note = category
                )
                repository.deleteExpense(expenseToDelete)
                _saveResult.value = Resource.Success("Đã xóa khoản chi tiêu!")
            } catch (e: Exception) {
                _saveResult.value = Resource.Error("Lỗi khi xóa: ${e.localizedMessage}")
            }
        }
    }
}
