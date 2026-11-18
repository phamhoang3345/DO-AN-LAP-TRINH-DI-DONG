package com.example.expensemanager.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.expensemanager.common.Resource
import com.example.expensemanager.databinding.ActivityAddExpenseBinding
import com.example.expensemanager.viewmodel.AddExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private val viewModel: AddExpenseViewModel by viewModels()

    private var expenseId: Int = 0
    private val calendar: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    companion object {
        const val EXTRA_EXPENSE_ID = "extra_expense_id"
        const val EXTRA_EXPENSE_TITLE = "extra_expense_title"
        const val EXTRA_EXPENSE_AMOUNT = "extra_expense_amount"
        const val EXTRA_EXPENSE_DATE = "extra_expense_date"
        const val EXTRA_EXPENSE_CATEGORY = "extra_expense_category"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        parseIntentData()
        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun parseIntentData() {
        val idStr = intent.getStringExtra(EXTRA_EXPENSE_ID) ?: intent.getIntExtra(EXTRA_EXPENSE_ID, 0).toString()
        expenseId = idStr.toIntOrNull() ?: 0

        val title = intent.getStringExtra(EXTRA_EXPENSE_TITLE)
        val amount = intent.getStringExtra(EXTRA_EXPENSE_AMOUNT)
        val date = intent.getStringExtra(EXTRA_EXPENSE_DATE)
        val category = intent.getStringExtra(EXTRA_EXPENSE_CATEGORY)

        if (expenseId != 0 && !title.isNullOrEmpty()) {
            binding.toolbar.title = "Sửa khoản chi tiêu"
            binding.etTitle.setText(title)
            val cleanAmount = amount?.replace("đ", "")?.replace("+", "")?.replace("-", "")?.replace(".", "")?.trim() ?: ""
            binding.etAmount.setText(cleanAmount)
            binding.etDate.setText(date ?: dateFormat.format(calendar.time))
            binding.etCategory.setText(category ?: "")
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            binding.toolbar.title = "Thêm khoản chi tiêu"
            binding.etDate.setText(dateFormat.format(calendar.time))
            binding.btnDelete.visibility = View.GONE
        }
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        // Mở DatePicker khi nhấn vào ô Ngày
        binding.etDate.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                binding.etDate.setText(dateFormat.format(calendar.time))
            }, year, month, day)

            datePickerDialog.show()
        }

        // Nút Lưu
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text?.toString()?.trim().orEmpty()
            val amountStr = binding.etAmount.text?.toString()?.trim().orEmpty()
            val date = binding.etDate.text?.toString()?.trim().orEmpty()
            val category = binding.etCategory.text?.toString()?.trim().orEmpty()

            viewModel.saveExpense(expenseId, title, amountStr, date, category)
        }

        // Nút Xóa
        binding.btnDelete.setOnClickListener {
            val title = binding.etTitle.text?.toString()?.trim().orEmpty()
            val amountStr = binding.etAmount.text?.toString()?.trim().orEmpty()
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            val date = binding.etDate.text?.toString()?.trim().orEmpty()
            val category = binding.etCategory.text?.toString()?.trim().orEmpty()

            viewModel.deleteExpense(expenseId, title, amount, date, category)
        }
    }

    private fun observeViewModel() {
        viewModel.saveResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                    binding.btnDelete.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    binding.btnDelete.isEnabled = true
                    Toast.makeText(this, resource.data, Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    binding.btnDelete.isEnabled = true
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
