package com.example.expensemanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.expensemanager.model.UserProfile

class ProfileViewModel : ViewModel() {

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> get() = _userProfile

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        _userProfile.value = UserProfile(
            name = "Nguyen Ngoc Huy",
            email = "huy.2474802015199@vanlanguni.vn"
        )
    }
}
