package com.example.expensemanager.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensemanager.adapter.CategoryAdapter
import com.example.expensemanager.common.Resource
import com.example.expensemanager.databinding.ActivityCategoryBinding
import com.example.expensemanager.viewmodel.CategoryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private val viewModel: CategoryViewModel by viewModels()
    private lateinit var categoryAdapter: CategoryAdapter

    companion object {
        const val EXTRA_SELECTED_CATEGORY_NAME = "extra_selected_category_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.recyclerCategories.layoutManager = LinearLayoutManager(this)
        categoryAdapter = CategoryAdapter(emptyList())
        binding.recyclerCategories.adapter = categoryAdapter
    }

    private fun setupListeners() {
        // Chọn danh mục -> Trả kết quả về cho AddExpenseActivity
        categoryAdapter.onItemClick = { category ->
            val resultIntent = Intent().apply {
                putExtra(EXTRA_SELECTED_CATEGORY_NAME, category.name)
            }
            setResult(RESULT_OK, resultIntent)
            Toast.makeText(this, "Đã chọn danh mục: ${category.name}", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Xóa danh mục
        categoryAdapter.onDeleteClick = { category ->
            AlertDialog.Builder(this)
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có chắc chắn muốn xóa danh mục \"${category.name}\" này không?")
                .setPositiveButton("Xóa") { _, _ ->
                    viewModel.deleteCategory(category)
                }
                .setNegativeButton("Hủy", null)
                .show()
        }

        // Nút Thêm danh mục mới
        binding.fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun observeViewModel() {
        // Collect Flow danh mục từ Room DB
        lifecycleScope.launch {
            viewModel.categoriesState.collectLatest { categories ->
                categoryAdapter.updateData(categories)
            }
        }

        viewModel.actionState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, resource.data, Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showAddCategoryDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }

        val etIcon = EditText(this).apply {
            hint = "Biểu tượng Emoji (Ví dụ: ☕, 🎮)"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        val etName = EditText(this).apply {
            hint = "Tên danh mục (*)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }

        layout.addView(etIcon)
        layout.addView(etName)

        AlertDialog.Builder(this)
            .setTitle("Thêm danh mục mới")
            .setView(layout)
            .setPositiveButton("Thêm") { dialog, _ ->
                val icon = etIcon.text.toString().trim().ifEmpty { "🏷️" }
                val name = etName.text.toString().trim()
                viewModel.addCategory(name, icon)
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
