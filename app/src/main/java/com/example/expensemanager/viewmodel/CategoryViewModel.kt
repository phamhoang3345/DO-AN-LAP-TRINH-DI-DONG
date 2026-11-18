package com.example.expensemanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.common.Resource
import com.example.expensemanager.data.local.AppDatabase
import com.example.expensemanager.data.local.dao.CategoryDao
import com.example.expensemanager.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val categoryDao: CategoryDao = AppDatabase.getDatabase(application).categoryDao()

    val categoriesState: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    private val _actionState = MutableLiveData<Resource<String>>()
    val actionState: LiveData<Resource<String>> get() = _actionState

    fun addCategory(name: String, icon: String? = "🏷️") {
        if (name.isBlank()) {
            _actionState.value = Resource.Error("Vui lòng nhập tên danh mục!")
            return
        }
        _actionState.value = Resource.Loading
        viewModelScope.launch {
            try {
                categoryDao.insertCategory(CategoryEntity(name = name.trim(), icon = icon ?: "🏷️"))
                _actionState.value = Resource.Success("Thêm danh mục thành công!")
            } catch (e: Exception) {
                _actionState.value = Resource.Error("Lỗi: ${e.localizedMessage}")
            }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        _actionState.value = Resource.Loading
        viewModelScope.launch {
            try {
                categoryDao.deleteCategory(category)
                _actionState.value = Resource.Success("Đã xóa danh mục!")
            } catch (e: Exception) {
                _actionState.value = Resource.Error("Lỗi khi xóa danh mục: ${e.localizedMessage}")
            }
        }
    }
}
