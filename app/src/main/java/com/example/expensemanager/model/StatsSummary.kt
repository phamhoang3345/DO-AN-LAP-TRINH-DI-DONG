package com.example.expensemanager.model

data class StatsSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val netBalance: Double,
    val avgDailyExpense: Double,
    val formattedIncome: String,
    val formattedExpense: String,
    val formattedNetBalance: String,
    val formattedAvgDailyExpense: String,
    val formattedChartTotal: String,
    val categoryStats: List<CategoryStat>
)
