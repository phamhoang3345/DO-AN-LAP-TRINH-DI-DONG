package com.example.expensemanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.common.Resource
import com.example.expensemanager.data.local.AppDatabase
import com.example.expensemanager.model.Expense
import com.example.expensemanager.repository.ExpenseRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository

    private val _expensesState = MutableLiveData<Resource<List<Expense>>>()
    val expensesState: LiveData<Resource<List<Expense>>> get() = _expensesState

    init {
        val expenseDao = AppDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(expenseDao)
        
        // 1. Observe Flow từ Local DB
        observeLocalExpenses()
        
        // 2. Kích hoạt sync dữ liệu từ API về Local DB
        syncExpenses()
    }

    private fun observeLocalExpenses() {
        viewModelScope.launch {
            repository.getExpensesFlow().collectLatest { resource ->
                _expensesState.value = resource
            }
        }
    }

    fun syncExpenses() {
        viewModelScope.launch {
            repository.syncExpensesFromApi()
        }
    }
    
    // Đổi tên hàm thành loadExpenses để tương thích với Fragment cũ gọi
    fun loadExpenses() {
        syncExpenses()
    }
}
