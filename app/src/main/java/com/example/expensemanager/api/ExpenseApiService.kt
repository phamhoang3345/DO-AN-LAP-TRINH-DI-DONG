package com.example.expensemanager.api

import com.example.expensemanager.model.Expense
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ExpenseApiService {

    @GET("expenses")
    suspend fun getExpenses(): Response<List<Expense>>

    @POST("expenses")
    suspend fun createExpense(@Body expense: Expense): Response<Expense>
}
