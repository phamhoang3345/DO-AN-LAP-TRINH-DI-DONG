package com.example.expensemanager.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensemanager.R
import com.example.expensemanager.adapter.CategoryStatAdapter
import com.example.expensemanager.databinding.FragmentStatsBinding
import com.example.expensemanager.ui.view.DonutSliceData
import com.example.expensemanager.viewmodel.BarTimeScale
import com.example.expensemanager.viewmodel.PeriodFilter
import com.example.expensemanager.viewmodel.StatsViewModel
import com.google.android.material.chip.Chip

class StatsFragment : Fragment() {

    private val viewModel: StatsViewModel by viewModels()

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryStatAdapter: CategoryStatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        categoryStatAdapter = CategoryStatAdapter()
        binding.rvCategoryStats.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryStatAdapter
        }
    }

    private fun setupListeners() {
        // Bộ lọc thời gian tổng quan
        binding.togglePeriod.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_this_month -> viewModel.setPeriodFilter(PeriodFilter.THIS_MONTH)
                    R.id.btn_last_month -> viewModel.setPeriodFilter(PeriodFilter.LAST_MONTH)
                    R.id.btn_all_time -> viewModel.setPeriodFilter(PeriodFilter.ALL_TIME)
                }
            }
        }

        // Chế độ xem biểu đồ cột: Tuần, Tháng, Năm
        binding.toggleBarScale.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_bar_week -> viewModel.setBarTimeScale(BarTimeScale.WEEK)
                    R.id.btn_bar_month -> viewModel.setBarTimeScale(BarTimeScale.MONTH)
                    R.id.btn_bar_year -> viewModel.setBarTimeScale(BarTimeScale.YEAR)
                }
            }
        }
    }

    private fun observeViewModel() {
        // 1. Lắng nghe dữ liệu Biểu đồ cột Thu - Chi theo thời gian
        viewModel.barChartData.observe(viewLifecycleOwner) { barDataList ->
            binding.barChartView.setData(barDataList)
            if (barDataList.isNotEmpty()) {
                binding.cardBarChartContainer.visibility = View.VISIBLE
            }
        }

        // 2. Lắng nghe dữ liệu Tổng quan & Biểu đồ Donut Danh mục
        viewModel.statsSummary.observe(viewLifecycleOwner) { summary ->
            binding.tvTotalExpense.text = summary.formattedExpense
            binding.tvTotalIncome.text = summary.formattedIncome
            binding.tvNetBalance.text = summary.formattedNetBalance
            binding.tvAvgDaily.text = summary.formattedAvgDailyExpense

            binding.tvChartTotalAmount.text = summary.formattedChartTotal

            // Cập nhật Biểu đồ Donut Chart (Biểu đồ tròn)
            val donutSlices = if (summary.categoryStats.isNotEmpty()) {
                summary.categoryStats.map { cat ->
                    DonutSliceData(
                        categoryName = cat.categoryName,
                        value = cat.totalAmount.toFloat(),
                        color = cat.color
                    )
                }
            } else {
                listOf(DonutSliceData("Chưa có chi tiêu", 1f, Color.parseColor("#E0E0E0")))
            }
            binding.donutChartView.setData(donutSlices)

            // Tạo các Chú thích Màu sắc (Legend Chips)
            binding.chipGroupLegend.removeAllViews()
            for (cat in summary.categoryStats) {
                val chip = Chip(requireContext()).apply {
                    text = cat.categoryName
                    chipIcon = ContextCompat.getDrawable(requireContext(), R.drawable.shape_circle)
                    chipIconTint = ColorStateList.valueOf(cat.color)
                    isChipIconVisible = true
                    isCheckable = false
                    isClickable = false
                    chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#F5F5F5"))
                    setTextColor(Color.parseColor("#263B32"))
                    textSize = 12f
                }
                binding.chipGroupLegend.addView(chip)
            }

            // Cập nhật danh sách chi tiết
            categoryStatAdapter.submitList(summary.categoryStats)

            binding.cardChartContainer.visibility = View.VISIBLE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private inline fun Chip.applies(block: Chip.() -> Unit): Chip {
    return this.apply(block)
}
