package com.example.expensemanager.repository

import com.example.expensemanager.model.Expense

class ExpenseRepository {
    fun getExpenses(): List<Expense> {
        return listOf(
            Expense(1, "Ăn sáng phở bò", "45.000 đ", "12/10/2026"),
            Expense(2, "Đổ xăng xe máy", "50.000 đ", "12/10/2026"),
            Expense(3, "Cà phê sữa đá", "25.000 đ", "11/10/2026")
        )
    }
}