package com.example.expensemanager.viewmodel

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.common.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val _loginState = MutableLiveData<Resource<Boolean>>()
    val loginState: LiveData<Resource<Boolean>> get() = _loginState

    fun login(email: String, password: String) {
        val cleanEmail = email.lowercase(Locale.ROOT).trim()
        val cleanPassword = password.trim()

        if (cleanEmail.isBlank()) {
            _loginState.value = Resource.Error("Vui lòng nhập Email!")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _loginState.value = Resource.Error("Email không đúng định dạng!")
            return
        }
        if (cleanPassword.isBlank()) {
            _loginState.value = Resource.Error("Vui lòng nhập Mật khẩu!")
            return
        }

        _loginState.value = Resource.Loading
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(cleanEmail, cleanPassword).await()
                _loginState.value = Resource.Success(true)
            } catch (e: Exception) {
                val errorDetails = e.localizedMessage ?: e.message ?: "Lỗi kết nối Firebase"
                _loginState.value = Resource.Error("Đăng nhập thất bại: $errorDetails")
            }
        }
    }

    fun signUp(email: String, password: String) {
        val cleanEmail = email.lowercase(Locale.ROOT).trim()
        val cleanPassword = password.trim()

        if (cleanEmail.isBlank()) {
            _loginState.value = Resource.Error("Vui lòng nhập Email để đăng ký!")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _loginState.value = Resource.Error("Email không đúng định dạng!")
            return
        }
        if (cleanPassword.isBlank()) {
            _loginState.value = Resource.Error("Vui lòng nhập Mật khẩu!")
            return
        }
        if (cleanPassword.length < 8) {
            _loginState.value = Resource.Error("Mật khẩu phải có ít nhất 8 ký tự!")
            return
        }

        _loginState.value = Resource.Loading
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword).await()
                _loginState.value = Resource.Success(true)
            } catch (e: Exception) {
                val errorDetails = e.localizedMessage ?: e.message ?: "Lỗi kết nối Firebase"
                _loginState.value = Resource.Error("Đăng ký thất bại: $errorDetails")
            }
        }
    }
}
