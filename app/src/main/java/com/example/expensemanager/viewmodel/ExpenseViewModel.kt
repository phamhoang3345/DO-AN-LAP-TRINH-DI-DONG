package com.example.expensemanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.expensemanager.model.Expense
import com.example.expensemanager.repository.ExpenseRepository

class ExpenseViewModel : ViewModel() {
    private val repository = ExpenseRepository()

    private val _expenseList = MutableLiveData<List<Expense>>()
    val expenseList: LiveData<List<Expense>> get() = _expenseList

    fun loadExpenses() {
        _expenseList.value = repository.getExpenses()
    }
}