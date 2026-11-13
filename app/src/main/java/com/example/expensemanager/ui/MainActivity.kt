package com.example.expensemanager.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.example.expensemanager.R
import com.example.expensemanager.viewmodel.ExpenseViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var expenseListView: TextView

    // Khởi tạo ViewModel theo chuẩn Android KTX
    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        expenseListView = findViewById(R.id.tvExpenseList)

        // 4. Lắng nghe dữ liệu (LiveData) từ ViewModel
        viewModel.expenseList.observe(this) { expenses ->
            // Khi có dữ liệu, hiển thị lên TextView thông qua biến binding (không cần findViewById)
            expenseListView.text = expenses.joinToString(separator = "\n") { expense ->
                "${expense.title} - ${expense.amount} (${expense.date})"
            }
        }

        // 5. Yêu cầu ViewModel tải dữ liệu lên
        viewModel.loadExpenses()
    }
}