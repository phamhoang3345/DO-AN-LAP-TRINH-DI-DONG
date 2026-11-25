package com.example.expensemanager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expensemanager.data.local.dao.CategoryDao
import com.example.expensemanager.data.local.dao.ExpenseDao
import com.example.expensemanager.data.local.dao.IncomeDao
import com.example.expensemanager.data.local.entity.CategoryEntity
import com.example.expensemanager.data.local.entity.ExpenseEntity
import com.example.expensemanager.data.local.entity.IncomeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ExpenseEntity::class, CategoryEntity::class, IncomeEntity::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun incomeDao(): IncomeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_manager_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate danh mục mặc định khi tạo DB lần đầu
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val defaultCategories = listOf(
                                    CategoryEntity(name = "Ăn uống", icon = "🍔"),
                                    CategoryEntity(name = "Di chuyển", icon = "🚗"),
                                    CategoryEntity(name = "Mua sắm", icon = "🛍️"),
                                    CategoryEntity(name = "Giải trí", icon = "🎮"),
                                    CategoryEntity(name = "Hóa đơn & Tiện ích", icon = "💡"),
                                    CategoryEntity(name = "Y tế & Sức khỏe", icon = "💊"),
                                    CategoryEntity(name = "Khác", icon = "📌")
                                )
                                for (cat in defaultCategories) {
                                    database.categoryDao().insertCategory(cat)
                                }
                            }
                        }
                    }
                })
                .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
