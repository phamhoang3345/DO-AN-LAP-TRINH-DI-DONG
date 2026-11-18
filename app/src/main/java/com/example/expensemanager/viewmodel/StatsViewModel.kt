package com.example.expensemanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StatsViewModel : ViewModel() {

    private val _monthlyTotal = MutableLiveData<String>()
    val monthlyTotal: LiveData<String> get() = _monthlyTotal

    private val _chartNote = MutableLiveData<String>()
    val chartNote: LiveData<String> get() = _chartNote

    init {
        loadStatsData()
    }

    fun loadStatsData() {
        _monthlyTotal.value = "2.500.000 đ"
        _chartNote.value = "Biểu đồ phân tích chi tiết sẽ được cập nhật ở phiên bản tiếp theo."
    }
}
