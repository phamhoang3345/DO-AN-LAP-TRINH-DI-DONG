package com.example.expensemanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.common.Resource
import com.example.expensemanager.data.local.AppDatabase
import com.example.expensemanager.data.local.SessionManager
import com.example.expensemanager.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val sessionManager = SessionManager(application)

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> get() = _userProfile

    private val _clearDataResult = MutableLiveData<Resource<String>>()
    val clearDataResult: LiveData<Resource<String>> get() = _clearDataResult

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        _userProfile.value = UserProfile(
            name = sessionManager.getUserName(),
            email = sessionManager.getUserEmail()
        )
    }

    fun clearAllData() {
        _clearDataResult.value = Resource.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Xóa toàn bộ dữ liệu trong Room DB (Expenses & Incomes)
                db.expenseDao().deleteAllExpenses()
                db.incomeDao().deleteAllIncomes()

                // 2. Reset số dư và tổng thu về 0
                sessionManager.saveBalance(0.0)
                sessionManager.saveTotalIncome(0.0)

                _clearDataResult.postValue(Resource.Success("Đã xóa toàn bộ dữ liệu ứng dụng thành công!"))
            } catch (e: Exception) {
                _clearDataResult.postValue(Resource.Error("Lỗi xóa dữ liệu: ${e.localizedMessage}"))
            }
        }
    }
}
