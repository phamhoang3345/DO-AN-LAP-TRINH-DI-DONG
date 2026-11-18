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

class ExpenseDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository

    private val _deleteResult = MutableLiveData<Resource<String>>()
    val deleteResult: LiveData<Resource<String>> get() = _deleteResult

    init {
        val expenseDao = AppDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(expenseDao)
    }

    fun deleteExpense(id: Int, title: String, amount: Double, date: String, category: String?) {
        _deleteResult.value = Resource.Loading
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
                _deleteResult.value = Resource.Success("Đã xóa khoản chi tiêu thành công!")
            } catch (e: Exception) {
                _deleteResult.value = Resource.Error("Lỗi khi xóa: ${e.localizedMessage}")
            }
        }
    }
}
