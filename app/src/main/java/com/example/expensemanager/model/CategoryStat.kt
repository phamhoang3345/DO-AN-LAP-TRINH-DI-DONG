package com.example.expensemanager.model

data class CategoryStat(
    val categoryName: String,
    val icon: String,
    val totalAmount: Double,
    val formattedAmount: String,
    val percentage: Float,
    val transactionCount: Int,
    val color: Int = 0
)
