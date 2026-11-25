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

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _loginState = MutableLiveData<Resource<Boolean>>()
    val loginState: LiveData<Resource<Boolean>> get() = _loginState

    fun login(email: String, password: String) {
        if (email.isBlank()) {
            _loginState.value = Resource.Error("Vui lòng nhập Email!")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginState.value = Resource.Error("Email không đúng định dạng!")
            return
        }
        if (password.isBlank()) {
            _loginState.value = Resource.Error("Vui lòng nhập Mật khẩu!")
            return
        }
        if (password.length < 6) {
            _loginState.value = Resource.Error("Mật khẩu phải có ít nhất 6 ký tự!")
            return
        }

        _loginState.value = Resource.Loading
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _loginState.value = Resource.Success(true)
            } catch (e: Exception) {
                _loginState.value = Resource.Error("Đăng nhập thất bại: ${e.localizedMessage}")
            }
        }
    }

    fun signUp(email: String, password: String) {
        if (email.isBlank()) {
            _loginState.value = Resource.Error("Vui lòng nhập Email để đăng ký!")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginState.value = Resource.Error("Email không đúng định dạng!")
            return
        }
        if (password.isBlank()) {
            _loginState.value = Resource.Error("Vui lòng nhập Mật khẩu!")
            return
        }
        if (password.length < 6) {
            _loginState.value = Resource.Error("Mật khẩu phải có ít nhất 6 ký tự!")
            return
        }

        _loginState.value = Resource.Loading
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _loginState.value = Resource.Success(true)
            } catch (e: Exception) {
                _loginState.value = Resource.Error("Đăng ký thất bại: ${e.localizedMessage}")
            }
        }
    }
}
