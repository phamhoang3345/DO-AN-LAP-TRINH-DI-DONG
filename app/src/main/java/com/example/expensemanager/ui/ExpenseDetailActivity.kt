package com.example.expensemanager.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.expensemanager.common.Resource
import com.example.expensemanager.databinding.ActivityExpenseDetailBinding
import com.example.expensemanager.viewmodel.ExpenseDetailViewModel

class ExpenseDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpenseDetailBinding
    private val viewModel: ExpenseDetailViewModel by viewModels()

    private var expenseId: Int = 0
    private var expenseTitle: String = ""
    private var expenseAmountStr: String = ""
    private var expenseDate: String = ""
    private var expenseCategory: String = ""

    companion object {
        const val EXTRA_EXPENSE_ID = "extra_expense_id"
        const val EXTRA_EXPENSE_TITLE = "extra_expense_title"
        const val EXTRA_EXPENSE_AMOUNT = "extra_expense_amount"
        const val EXTRA_EXPENSE_DATE = "extra_expense_date"
        const val EXTRA_EXPENSE_CATEGORY = "extra_expense_category"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        parseIntentData()
        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun parseIntentData() {
        val idStr = intent.getStringExtra(EXTRA_EXPENSE_ID) ?: intent.getIntExtra(EXTRA_EXPENSE_ID, 0).toString()
        expenseId = idStr.toIntOrNull() ?: 0

        expenseTitle = intent.getStringExtra(EXTRA_EXPENSE_TITLE).orEmpty()
        expenseAmountStr = intent.getStringExtra(EXTRA_EXPENSE_AMOUNT).orEmpty()
        expenseDate = intent.getStringExtra(EXTRA_EXPENSE_DATE).orEmpty()
        expenseCategory = intent.getStringExtra(EXTRA_EXPENSE_CATEGORY).orEmpty()

        binding.tvDetailTitle.text = if (expenseTitle.isNotEmpty()) expenseTitle else "Chi tiêu không tên"
        val amountDisplay = if (expenseAmountStr.endsWith("đ")) expenseAmountStr else "$expenseAmountStr đ"
        binding.tvDetailAmount.text = amountDisplay
        binding.tvDetailDate.text = if (expenseDate.isNotEmpty()) expenseDate else "---"
        binding.tvDetailCategory.text = if (expenseCategory.isNotEmpty()) expenseCategory else "Khác"
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        // Nút Chỉnh sửa -> Mở AddExpenseActivity
        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java).apply {
                putExtra(AddExpenseActivity.EXTRA_EXPENSE_ID, expenseId)
                putExtra(AddExpenseActivity.EXTRA_EXPENSE_TITLE, expenseTitle)
                putExtra(AddExpenseActivity.EXTRA_EXPENSE_AMOUNT, expenseAmountStr)
                putExtra(AddExpenseActivity.EXTRA_EXPENSE_DATE, expenseDate)
                putExtra(AddExpenseActivity.EXTRA_EXPENSE_CATEGORY, expenseCategory)
            }
            startActivity(intent)
            finish()
        }

        // Nút Xóa -> Hỏi xác nhận trước khi xóa
        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa khoản chi tiêu \"$expenseTitle\" này không?")
                .setPositiveButton("Xóa") { _, _ ->
                    val cleanAmount = expenseAmountStr.replace("đ", "").replace("+", "").replace("-", "").replace(".", "").trim()
                    val amountDouble = cleanAmount.toDoubleOrNull() ?: 0.0
                    viewModel.deleteExpense(expenseId, expenseTitle, amountDouble, expenseDate, expenseCategory)
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    private fun observeViewModel() {
        viewModel.deleteResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnEdit.isEnabled = false
                    binding.btnDelete.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, resource.data, Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnEdit.isEnabled = true
                    binding.btnDelete.isEnabled = true
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
