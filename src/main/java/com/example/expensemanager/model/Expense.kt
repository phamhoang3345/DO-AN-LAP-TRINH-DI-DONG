package com.example.expensemanager.model

import com.google.gson.annotations.SerializedName

data class Expense(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("title")
    val title: String,
    @SerializedName("amount")
    val amount: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("category")
    val category: String? = null
)
