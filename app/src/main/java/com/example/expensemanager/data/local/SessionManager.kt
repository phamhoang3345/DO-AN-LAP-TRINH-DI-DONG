package com.example.expensemanager.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

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
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_BALANCE = "user_balance"
        private const val KEY_TOTAL_INCOME = "total_income"
        private const val KEY_BUDGET_LIMIT = "budget_limit"
        private const val KEY_DARK_MODE = "dark_mode"
    }

    fun saveSession(isLoggedIn: Boolean, email: String, name: String? = null, userId: String? = null) {
        val cleanEmail = email.trim().lowercase(Locale.ROOT)
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val resolvedUid = when {
            !userId.isNullOrBlank() -> userId
            firebaseUser != null && firebaseUser.email?.equals(cleanEmail, ignoreCase = true) == true -> firebaseUser.uid
            else -> cleanEmail
        }

        val resolvedName = when {
            !name.isNullOrBlank() -> name
            firebaseUser?.displayName?.isNotBlank() == true -> firebaseUser.displayName!!
            else -> cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        }

        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            putString(KEY_USER_EMAIL, cleanEmail)
            putString(KEY_USER_NAME, resolvedName)
            putString(KEY_USER_ID, resolvedUid)
            apply()
        }
    }

    fun getUserId(): String {
        val uid = prefs.getString(KEY_USER_ID, null)
        if (!uid.isNullOrBlank()) return uid

        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
        if (!firebaseUid.isNullOrBlank()) {
            prefs.edit().putString(KEY_USER_ID, firebaseUid).apply()
            return firebaseUid
        }

        val email = getUserEmail()
        if (email.isNotBlank()) return email

        return "guest"
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
        prefs.edit().clear().apply()
    }
}
