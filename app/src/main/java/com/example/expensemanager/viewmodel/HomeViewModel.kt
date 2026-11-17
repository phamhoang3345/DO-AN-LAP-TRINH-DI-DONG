package com.example.expensemanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _greeting = MutableLiveData<String>()
    val greeting: LiveData<String> get() = _greeting

    private val _currentBalance = MutableLiveData<String>()
    val currentBalance: LiveData<String> get() = _currentBalance

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        _greeting.value = "Xin chào! Chào mừng bạn đến với Expense Manager"
        _currentBalance.value = "2.500.000 đ"
    }
}
