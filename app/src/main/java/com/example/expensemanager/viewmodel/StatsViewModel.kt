package com.example.expensemanager.viewmodel

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.local.AppDatabase
import com.example.expensemanager.data.local.SessionManager
import com.example.expensemanager.data.local.entity.ExpenseEntity
import com.example.expensemanager.model.BarGroupData
import com.example.expensemanager.model.CategoryStat
import com.example.expensemanager.model.StatsSummary
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class PeriodFilter {
    THIS_MONTH,
    LAST_MONTH,
    ALL_TIME
}

enum class BarTimeScale {
    WEEK,
    MONTH,
    YEAR
}

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val expenseDao = db.expenseDao()
    private val categoryDao = db.categoryDao()
    private val sessionManager = SessionManager(application)

    private val _currentPeriod = MutableLiveData(PeriodFilter.THIS_MONTH)
    val currentPeriod: LiveData<PeriodFilter> get() = _currentPeriod

    private val _currentBarScale = MutableLiveData(BarTimeScale.MONTH)
    val currentBarScale: LiveData<BarTimeScale> get() = _currentBarScale

    private val _statsSummary = MutableLiveData<StatsSummary>()
    val statsSummary: LiveData<StatsSummary> get() = _statsSummary

    private val _barChartData = MutableLiveData<List<BarGroupData>>()
    val barChartData: LiveData<List<BarGroupData>> get() = _barChartData

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var allExpensesList: List<ExpenseEntity> = emptyList()
    private var categoryMap: Map<String, String> = emptyMap()

    private val paletteColors = listOf(
        Color.parseColor("#1565C0"), // Xanh dương
        Color.parseColor("#2E7D32"), // Xanh lá
        Color.parseColor("#EF6C00"), // Cam
        Color.parseColor("#6A1B9A"), // Tím
        Color.parseColor("#F57F17"), // Vàng cam
        Color.parseColor("#00838F"), // Xanh ngọc
        Color.parseColor("#C62828"), // Đỏ
        Color.parseColor("#D84315"), // Cam đỏ
        Color.parseColor("#4E342E"), // Nâu
        Color.parseColor("#37474F")  // Xám xanh
    )

    init {
        observeData()
    }

    fun setPeriodFilter(filter: PeriodFilter) {
        _currentPeriod.value = filter
        calculateStats()
    }

    fun setBarTimeScale(scale: BarTimeScale) {
        _currentBarScale.value = scale
        calculateBarChartData()
    }

    private fun observeData() {
        viewModelScope.launch {
            _isLoading.value = true
            val currentUserId = sessionManager.getUserId()
            combine(
                expenseDao.getExpensesForUser(currentUserId),
                categoryDao.getAllCategories(),
            ) { expenses, categories ->
                Pair(expenses, categories)
            }.collect { (expenses, categories) ->
                allExpensesList = expenses
                categoryMap = categories.associateBy({ it.name }, { it.icon ?: "🏷️" })
                calculateStats()
                calculateBarChartData()
                _isLoading.value = false
            }
        }
    }

    private fun calculateStats() {
        val selectedPeriod = _currentPeriod.value ?: PeriodFilter.THIS_MONTH

        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentMonth = now.get(Calendar.MONTH)

        val filteredItems = allExpensesList.filter { item ->
            when (selectedPeriod) {
                PeriodFilter.ALL_TIME -> true
                PeriodFilter.THIS_MONTH -> isSameMonthYear(item.date, currentYear, currentMonth)
                PeriodFilter.LAST_MONTH -> {
                    val lastMonthCal = Calendar.getInstance().apply {
                        add(Calendar.MONTH, -1)
                    }
                    isSameMonthYear(item.date, lastMonthCal.get(Calendar.YEAR), lastMonthCal.get(Calendar.MONTH))
                }
            }
        }

        val (incomeList, expenseList) = filteredItems.partition { item ->
            item.type == "INCOME" || item.title.startsWith("Thu nhập") || (item.note?.contains("Thu nhập") == true)
        }

        val totalIncome = incomeList.sumOf { it.amount }
        val totalExpense = expenseList.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense

        val daysCount = when (selectedPeriod) {
            PeriodFilter.THIS_MONTH -> now.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
            PeriodFilter.LAST_MONTH -> {
                val lastMonthCal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
                lastMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            }
            PeriodFilter.ALL_TIME -> {
                val uniqueDates = expenseList.map { it.date.take(10) }.distinct().size
                uniqueDates.coerceAtLeast(1)
            }
        }
        val avgDailyExpense = if (daysCount > 0) totalExpense / daysCount else 0.0

        // CHỈ LẤY DỮ LIỆU TỪ CHI TIÊU (Expense) CHO BIỂU ĐỒ TRÒN & DANH MỤC CHI TIẾT
        val chartItems = expenseList
        val chartTotalSum = chartItems.sumOf { it.amount }

        val groupedByCategory = chartItems.groupBy { item ->
            val noteName = item.note?.trim().orEmpty().ifEmpty { item.title.trim() }
            noteName.ifEmpty { "Khác" }
        }

        val categoryStatsList = groupedByCategory.entries.mapIndexed { index, entry ->
            val catName = entry.key
            val items = entry.value
            val catTotal = items.sumOf { it.amount }
            val percentage = if (chartTotalSum > 0) ((catTotal / chartTotalSum) * 100).toFloat() else 0f
            val icon = categoryMap[catName] ?: getIconForCategory(catName)
            val color = paletteColors[index % paletteColors.size]

            CategoryStat(
                categoryName = catName,
                icon = icon,
                totalAmount = catTotal,
                formattedAmount = formatCurrency(catTotal),
                percentage = percentage,
                transactionCount = items.size,
                color = color
            )
        }.sortedByDescending { it.totalAmount }

        _statsSummary.value = StatsSummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = netBalance,
            avgDailyExpense = avgDailyExpense,
            formattedIncome = "+ ${formatCurrency(totalIncome)}",
            formattedExpense = "- ${formatCurrency(totalExpense)}",
            formattedNetBalance = formatCurrency(netBalance),
            formattedAvgDailyExpense = "${formatCurrency(avgDailyExpense)}/ngày",
            formattedChartTotal = formatCurrency(chartTotalSum),
            categoryStats = categoryStatsList
        )
    }

    private fun calculateBarChartData() {
        val scale = _currentBarScale.value ?: BarTimeScale.MONTH
        val result = mutableListOf<BarGroupData>()

        when (scale) {
            BarTimeScale.MONTH -> {
                val monthList = mutableListOf<Pair<Int, Int>>()
                for (i in 5 downTo 0) {
                    val cal = Calendar.getInstance().apply {
                        add(Calendar.MONTH, -i)
                    }
                    monthList.add(Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)))
                }

                for ((year, month) in monthList) {
                    val itemsInMonth = allExpensesList.filter { isSameMonthYear(it.date, year, month) }
                    val (incList, expList) = itemsInMonth.partition {
                        it.type == "INCOME" || it.title.startsWith("Thu nhập") || (itemNoteIsIncome(it.note))
                    }
                    val inc = incList.sumOf { it.amount }
                    val exp = expList.sumOf { it.amount }

                    result.add(
                        BarGroupData(
                            label = "T${month + 1}",
                            income = inc,
                            expense = exp,
                            formattedIncome = formatShortCurrency(inc),
                            formattedExpense = formatShortCurrency(exp)
                        )
                    )
                }
            }

            BarTimeScale.WEEK -> {
                val dayList = mutableListOf<Calendar>()
                for (i in 6 downTo 0) {
                    val cal = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -i)
                    }
                    dayList.add(cal)
                }

                val dayNames = arrayOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
                for (cal in dayList) {
                    val y = cal.get(Calendar.YEAR)
                    val m = cal.get(Calendar.MONTH)
                    val d = cal.get(Calendar.DAY_OF_MONTH)

                    val itemsOnDay = allExpensesList.filter { isSameDate(it.date, y, m, d) }
                    val (incList, expList) = itemsOnDay.partition {
                        it.type == "INCOME" || it.title.startsWith("Thu nhập") || (itemNoteIsIncome(it.note))
                    }
                    val inc = incList.sumOf { it.amount }
                    val exp = expList.sumOf { it.amount }

                    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
                    val dayLabel = dayNames[dayOfWeek.coerceIn(0, 6)]

                    result.add(
                        BarGroupData(
                            label = dayLabel,
                            income = inc,
                            expense = exp,
                            formattedIncome = formatShortCurrency(inc),
                            formattedExpense = formatShortCurrency(exp)
                        )
                    )
                }
            }

            BarTimeScale.YEAR -> {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                for (year in (currentYear - 2)..currentYear) {
                    val itemsInYear = allExpensesList.filter { getYearFromDate(it.date) == year }
                    val (incList, expList) = itemsInYear.partition {
                        it.type == "INCOME" || it.title.startsWith("Thu nhập") || (itemNoteIsIncome(it.note))
                    }
                    val inc = incList.sumOf { it.amount }
                    val exp = expList.sumOf { it.amount }

                    result.add(
                        BarGroupData(
                            label = year.toString(),
                            income = inc,
                            expense = exp,
                            formattedIncome = formatShortCurrency(inc),
                            formattedExpense = formatShortCurrency(exp)
                        )
                    )
                }
            }
        }

        _barChartData.value = result
    }

    private fun itemNoteIsIncome(note: String?): Boolean {
        return note?.contains("Thu nhập") == true
    }

    private fun isSameMonthYear(dateStr: String, targetYear: Int, targetMonth: Int): Boolean {
        if (dateStr.isBlank()) return true
        val formats = listOf(
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "yyyy/MM/dd",
            "dd-MM-yyyy",
            "yyyy-M-d",
            "d/M/yyyy",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                sdf.isLenient = true
                val date = sdf.parse(dateStr.trim())
                if (date != null) {
                    val cal = Calendar.getInstance().apply { time = date }
                    return cal.get(Calendar.YEAR) == targetYear && cal.get(Calendar.MONTH) == targetMonth
                }
            } catch (_: Exception) {}
        }
        return true
    }

    private fun isSameDate(dateStr: String, targetYear: Int, targetMonth: Int, targetDay: Int): Boolean {
        if (dateStr.isBlank()) return false
        val formats = listOf(
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "yyyy/MM/dd",
            "dd-MM-yyyy"
        )
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                sdf.isLenient = true
                val date = sdf.parse(dateStr.trim())
                if (date != null) {
                    val cal = Calendar.getInstance().apply { time = date }
                    return cal.get(Calendar.YEAR) == targetYear &&
                            cal.get(Calendar.MONTH) == targetMonth &&
                            cal.get(Calendar.DAY_OF_MONTH) == targetDay
                }
            } catch (_: Exception) {}
        }
        return false
    }

    private fun getYearFromDate(dateStr: String): Int {
        val formats = listOf("yyyy-MM-dd", "dd/MM/yyyy", "yyyy/MM/dd")
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                val date = sdf.parse(dateStr.trim())
                if (date != null) {
                    val cal = Calendar.getInstance().apply { time = date }
                    return cal.get(Calendar.YEAR)
                }
            } catch (_: Exception) {}
        }
        return Calendar.getInstance().get(Calendar.YEAR)
    }

    private fun getIconForCategory(categoryName: String): String {
        return when {
            categoryName.contains("Ăn uống", ignoreCase = true) -> "🍔"
            categoryName.contains("Di chuyển", ignoreCase = true) -> "🚗"
            categoryName.contains("Mua sắm", ignoreCase = true) -> "🛍️"
            categoryName.contains("Giải trí", ignoreCase = true) -> "🎮"
            categoryName.contains("Hóa đơn", ignoreCase = true) || categoryName.contains("Tiện ích", ignoreCase = true) -> "💡"
            categoryName.contains("Y tế", ignoreCase = true) || categoryName.contains("Sức khỏe", ignoreCase = true) -> "💊"
            categoryName.contains("Thu nhập", ignoreCase = true) || categoryName.contains("Lương", ignoreCase = true) || categoryName.contains("Thưởng", ignoreCase = true) -> "💵"
            else -> "🏷️"
        }
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getInstance(Locale.GERMANY)
        return "${formatter.format(amount.toLong())} đ"
    }

    private fun formatShortCurrency(amount: Double): String {
        return when {
            amount >= 1_000_000 -> "${String.format(Locale.getDefault(), "%.1f", amount / 1_000_000)}Mđ"
            amount >= 1_000 -> "${(amount / 1_000).toInt()}kđ"
            else -> "${amount.toInt()}đ"
        }
    }
}
