package com.example.expensemanager.repository

import com.example.expensemanager.api.ApiClient
import com.example.expensemanager.common.Resource
import com.example.expensemanager.model.Expense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpenseRepository {

    suspend fun getExpenses(): Resource<List<Expense>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.apiService.getExpenses()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Dữ liệu từ máy chủ rỗng")
                }
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Lỗi HTTP 400: Yêu cầu không hợp lệ (Bad Request)"
                    401 -> "Lỗi HTTP 401: Chưa được xác thực (Unauthorized)"
                    404 -> "Lỗi HTTP 404: Không tìm thấy tài nguyên (Not Found)"
                    500 -> "Lỗi HTTP 500: Lỗi máy chủ nội bộ (Internal Server Error)"
                    else -> "Mã lỗi HTTP: ${response.code()}"
                }
                Resource.Error(errorMessage, response.code())
            }
        } catch (e: Exception) {
            Resource.Error("Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối Internet! (${e.localizedMessage})")
        }
    }
}
