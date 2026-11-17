package com.example.expensemanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.common.Resource
import com.example.expensemanager.model.Expense
import com.example.expensemanager.repository.ExpenseRepository
import kotlinx.coroutines.launch

class ExpenseViewModel : ViewModel() {

    private val repository = ExpenseRepository()

    private val _expensesState = MutableLiveData<Resource<List<Expense>>>()
    val expensesState: LiveData<Resource<List<Expense>>> get() = _expensesState

    init {
        loadExpenses()
    }

    fun loadExpenses() {
        _expensesState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.getExpenses()
            _expensesState.value = result
        }
    }
}
