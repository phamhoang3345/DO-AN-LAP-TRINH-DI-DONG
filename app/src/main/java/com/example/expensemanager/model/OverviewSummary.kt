package com.example.expensemanager.model

data class OverviewSummary(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0
) {
    val balance: Double
        get() = totalIncome - totalExpense
}