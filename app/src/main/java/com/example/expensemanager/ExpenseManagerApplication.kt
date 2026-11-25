package com.example.expensemanager

import android.app.Application
import com.google.firebase.FirebaseApp

class ExpenseManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
        } catch (_: Exception) {}
    }
}
