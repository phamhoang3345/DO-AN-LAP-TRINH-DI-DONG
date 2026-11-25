package com.example.expensemanager.viewmodel

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.common.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _registerState = MutableLiveData<Resource<String>>()
    val registerState: LiveData<Resource<String>> get() = _registerState

    fun register(name: String, email: String, pass: String, confirmPass: String, termsChecked: Boolean) {
        if (name.isBlank()) {
            _registerState.value = Resource.Error("Vui lòng nhập họ và tên!")
            return
        }
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registerState.value = Resource.Error("Email không hợp lệ!")
            return
        }
        if (pass.length < 8) {
            _registerState.value = Resource.Error("Mật khẩu phải có ít nhất 8 ký tự!")
            return
        }
        if (pass != confirmPass) {
            _registerState.value = Resource.Error("Mật khẩu xác nhận không khớp!")
            return
        }
        if (!termsChecked) {
            _registerState.value = Resource.Error("Bạn phải đồng ý với Điều khoản sử dụng!")
            return
        }

        _registerState.value = Resource.Loading
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val user = result.user
                if (user != null) {
                    val profileUpdates = userProfileChangeRequest {
                        displayName = name
                    }
                    user.updateProfile(profileUpdates).await()
                }
                _registerState.value = Resource.Success("Đăng ký tài khoản thành công!")
            } catch (e: Exception) {
                _registerState.value = Resource.Error("Đăng ký thất bại: ${e.localizedMessage}")
            }
        }
    }
}
