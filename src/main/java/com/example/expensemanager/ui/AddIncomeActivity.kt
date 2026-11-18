package com.example.expensemanager.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.expensemanager.common.Resource
import com.example.expensemanager.databinding.ActivityAddIncomeBinding
import com.example.expensemanager.viewmodel.AddIncomeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddIncomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddIncomeBinding
    private val viewModel: AddIncomeViewModel by viewModels()

    private val calendar: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        // Mặc định ngày nhận là ngày hiện tại
        binding.etDate.setText(dateFormat.format(calendar.time))
    }

    private fun setupListeners() {
        // Mở DatePicker khi nhấn vào ô Ngày nhận
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

        // Nút Lưu khoản thu
        binding.btnSave.setOnClickListener {
            val amountStr = binding.etAmount.text?.toString()?.trim().orEmpty()
            val source = binding.etSource.text?.toString()?.trim().orEmpty()
            val date = binding.etDate.text?.toString()?.trim().orEmpty()
            val note = binding.etNote.text?.toString()?.trim().orEmpty()

            viewModel.saveIncome(amountStr, source, date, note)
        }
    }

    private fun observeViewModel() {
        viewModel.saveResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(this, resource.data, Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
