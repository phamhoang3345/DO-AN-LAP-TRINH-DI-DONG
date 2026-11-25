package com.example.expensemanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.common.Resource
import com.example.expensemanager.data.local.AppDatabase
import com.example.expensemanager.data.local.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val expenseDao = AppDatabase.getDatabase(application).expenseDao()

    private val _greeting = MutableLiveData<String>()
    val greeting: LiveData<String> get() = _greeting

    private val _currentBalance = MutableLiveData<String>()
    val currentBalance: LiveData<String> get() = _currentBalance

    private val _totalIncome = MutableLiveData<String>()
    val totalIncome: LiveData<String> get() = _totalIncome

    private val _totalExpense = MutableLiveData<String>()
    val totalExpense: LiveData<String> get() = _totalExpense

    // Các LiveData cho Hạn mức chi tiêu (Budget Progress)
    private val _budgetDetail = MutableLiveData<String>()
    val budgetDetail: LiveData<String> get() = _budgetDetail

    private val _budgetPercent = MutableLiveData<Int>()
    val budgetPercent: LiveData<Int> get() = _budgetPercent

    private val _budgetStatusLevel = MutableLiveData<Int>() // 0 = xanh (<80%), 1 = cam (80-100%), 2 = đỏ (>100%)
    val budgetStatusLevel: LiveData<Int> get() = _budgetStatusLevel

    private val _budgetWarningMsg = MutableLiveData<String?>()
    val budgetWarningMsg: LiveData<String?> get() = _budgetWarningMsg

    private val _balanceActionState = MutableLiveData<Resource<String>>()
    val balanceActionState: LiveData<Resource<String>> get() = _balanceActionState

    private var baseIncomeAmount: Double = 0.0
    private var currentTotalExpense: Double = 0.0
    private var currentBudgetLimit: Double = 3000000.0

    init {
        loadHomeData()
        observeExpensesFromRoom()
    }

    fun loadHomeData() {
        _greeting.value = "Xin chào! Chào mừng bạn đến với Expense Manager"
        baseIncomeAmount = sessionManager.getTotalIncome()
        currentBudgetLimit = sessionManager.getBudgetLimit()
        updateBudgetCalculation()
    }

    private fun observeExpensesFromRoom() {
        viewModelScope.launch {
            val currentUserId = sessionManager.getUserId()
            expenseDao.getExpensesForUser(currentUserId).collectLatest { allItems ->
                val (incomeItems, expenseItems) = allItems.partition { item ->
                    item.type == "INCOME" || item.title.startsWith("Thu nhập") || (item.note?.contains("Thu nhập") == true)
                }

                // 1. Tính tổng chi tiêu
                currentTotalExpense = expenseItems.sumOf { it.amount }
                _totalExpense.value = "- ${formatCurrency(currentTotalExpense)}"

                // 2. Tính tổng thu nhập
                val totalIncomeSum = incomeItems.sumOf { it.amount }
                _totalIncome.value = "+ ${formatCurrency(totalIncomeSum)}"

                // 3. Tính số dư
                val calculatedBalance = totalIncomeSum - currentTotalExpense
                sessionManager.saveBalance(calculatedBalance)
                _currentBalance.value = formatCurrency(calculatedBalance)

                // 4. Cập nhật Tiến độ Ngân sách
                updateBudgetCalculation()
            }
        }
    }

    fun setBudgetLimit(limit: Double) {
        if (limit <= 0) {
            _balanceActionState.value = Resource.Error("Hạn mức chi tiêu phải lớn hơn 0!")
            return
        }
        currentBudgetLimit = limit
        sessionManager.saveBudgetLimit(limit)
        updateBudgetCalculation()
        _balanceActionState.value = Resource.Success("Đã cập nhật hạn mức chi tiêu thành ${formatCurrency(limit)}!")
    }

    private fun updateBudgetCalculation() {
        val percent = if (currentBudgetLimit > 0) {
            ((currentTotalExpense / currentBudgetLimit) * 100).toInt()
        } else {
            0
        }

        _budgetPercent.value = percent.coerceAtLeast(0)
        _budgetDetail.value = "Đã tiêu ${formatCurrency(currentTotalExpense)} / ${formatCurrency(currentBudgetLimit)} (${percent}%)"

        when {
            percent >= 100 -> {
                _budgetStatusLevel.value = 2 // Đỏ
                _budgetWarningMsg.value = "🚨 NGUY HIỂM: Chi tiêu đã VƯỢT QUÁ 100% hạn mức ngân sách!"
            }
            percent >= 80 -> {
                _budgetStatusLevel.value = 1 // Cam
                _budgetWarningMsg.value = "⚠️ CẢNH BÁO: Chi tiêu đã đạt ${percent}% hạn mức ngân sách!"
            }
            else -> {
                _budgetStatusLevel.value = 0 // Xanh
                _budgetWarningMsg.value = null
            }
        }
    }

    fun addMoney(amount: Double) {
        if (amount <= 0) {
            _balanceActionState.value = Resource.Error("Vui lòng nhập số tiền lớn hơn 0!")
            return
        }
        baseIncomeAmount += amount
        sessionManager.saveTotalIncome(baseIncomeAmount)
        _balanceActionState.value = Resource.Success("Đã thêm khoản thu ${formatCurrency(amount)}!")
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getInstance(Locale.GERMANY)
        return "${formatter.format(amount.toLong())} đ"
    }
}
