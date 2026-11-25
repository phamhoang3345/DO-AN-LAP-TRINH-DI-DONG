package com.example.expensemanager.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = run {
        var sharedPreferences: SharedPreferences? = null
        try {
            val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            sharedPreferences = EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Throwable) {
            // Fallback an toàn sang SharedPreferences chuẩn nếu thiết bị/emulator không hỗ trợ Keystore
            try {
                sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            } catch (_: Throwable) {}
        }
        sharedPreferences ?: context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREF_NAME = "expense_encrypted_user_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_BALANCE = "user_balance"
        private const val KEY_TOTAL_INCOME = "total_income"
        private const val KEY_BUDGET_LIMIT = "budget_limit"
        private const val KEY_DARK_MODE = "dark_mode"
    }

    fun saveSession(isLoggedIn: Boolean, email: String, name: String? = null) {
        val currentName = prefs.getString(KEY_USER_NAME, null)
        val resolvedName = when {
            !name.isNullOrBlank() -> name
            !currentName.isNullOrBlank() -> currentName
            else -> email.substringBefore("@").replaceFirstChar { it.uppercase() }
        }

        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, resolvedName)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserEmail(): String {
        return prefs.getString(KEY_USER_EMAIL, "giang.nguyen@example.com") ?: "giang.nguyen@example.com"
    }

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "Nguyen Ngan Giang") ?: "Nguyen Ngan Giang"
    }

    fun getBalance(): Double {
        return prefs.getFloat(KEY_USER_BALANCE, 0f).toDouble()
    }

    fun saveBalance(balance: Double) {
        prefs.edit().putFloat(KEY_USER_BALANCE, balance.toFloat()).apply()
    }

    fun getTotalIncome(): Double {
        return prefs.getFloat(KEY_TOTAL_INCOME, 0f).toDouble()
    }

    fun saveTotalIncome(income: Double) {
        prefs.edit().putFloat(KEY_TOTAL_INCOME, income.toFloat()).apply()
    }

    fun getBudgetLimit(): Double {
        return prefs.getFloat(KEY_BUDGET_LIMIT, 3000000f).toDouble()
    }

    fun saveBudgetLimit(limit: Double) {
        prefs.edit().putFloat(KEY_BUDGET_LIMIT, limit.toFloat()).apply()
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun clearSession() {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply()
    }
}
