package com.example.expensemanager.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.expensemanager.common.Resource
import com.example.expensemanager.data.local.SessionManager
import com.example.expensemanager.databinding.ActivityLoginBinding
import com.example.expensemanager.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim().orEmpty()
            val password = binding.etPassword.text?.toString()?.trim().orEmpty()
            viewModel.login(email, password)
        }

        binding.tvRegister.setOnClickListener {
            Toast.makeText(this, "Tính năng đăng ký đang được phát triển!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

                    val email = binding.etEmail.text?.toString()?.trim().orEmpty()
                    sessionManager.saveSession(isLoggedIn = true, email = email.ifEmpty { "giang.nguyen@example.com" })

                    // Chuyển sang MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
