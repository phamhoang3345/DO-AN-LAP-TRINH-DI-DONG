package com.example.expensemanager.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.expensemanager.data.local.SessionManager
import com.example.expensemanager.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Tích hợp Android 12+ Splash Screen API để ghi đè logo Android mặc định
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = SessionManager(this)

        // Kiểm tra phiên đăng nhập sau 1.5 giây
        lifecycleScope.launch {
            delay(1500)
            if (sessionManager.isLoggedIn()) {
                // Đã đăng nhập -> Chuyển thẳng vào MainActivity
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                // Chưa đăng nhập -> Chuyển sang LoginActivity
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            }
            finish()
        }
    }
}
