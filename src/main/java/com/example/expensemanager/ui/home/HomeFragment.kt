package com.example.expensemanager.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.expensemanager.common.Resource
import com.example.expensemanager.databinding.FragmentHomeBinding
import com.example.expensemanager.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.currentBalance.observe(viewLifecycleOwner) { amount ->
            binding.tvBalanceAmount.text = amount
        }

        // Lắng nghe Tổng Thu Tháng Này
        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            binding.tvTotalIncome.text = income
        }

        // Lắng nghe Tổng Chi Tháng Này
        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            binding.tvTotalExpense.text = expense
        }

        // Lắng nghe Chi tiết Hạn mức Chi tiêu (Ví dụ: Đã tiêu 1.500.000 đ / 3.000.000 đ - 50%)
        viewModel.budgetDetail.observe(viewLifecycleOwner) { detail ->
            binding.tvBudgetDetail.text = detail
        }

        // Lắng nghe % Tiến độ Hạn mức
        viewModel.budgetPercent.observe(viewLifecycleOwner) { percent ->
            binding.progressBudget.progress = percent.coerceIn(0, 100)
        }

        // Lắng nghe Mức độ cảnh báo để đổi màu thanh ProgressBar (Xanh -> Cam -> Đỏ)
        viewModel.budgetStatusLevel.observe(viewLifecycleOwner) { level ->
            val colorHex = when (level) {
                1 -> "#F57C00" // Cảnh báo sắp vượt (Cam)
                2 -> "#D32F2F" // Đã vượt (Đỏ)
                else -> "#388E3C" // Bình thường (Xanh lá)
            }
            binding.progressBudget.progressTintList = ColorStateList.valueOf(Color.parseColor(colorHex))
        }

        // Lắng nghe Thông báo cảnh báo vượt hạn mức
        viewModel.budgetWarningMsg.observe(viewLifecycleOwner) { msg ->
            if (msg.isNullOrEmpty()) {
                binding.tvBudgetWarning.visibility = View.GONE
            } else {
                binding.tvBudgetWarning.visibility = View.VISIBLE
                binding.tvBudgetWarning.text = msg
                val warningColor = if (msg.contains("NGUY HIỂM")) "#D32F2F" else "#F57C00"
                binding.tvBudgetWarning.setTextColor(Color.parseColor(warningColor))
            }
        }

        viewModel.balanceActionState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), resource.data, Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }

        // Nút Thiết lập Hạn mức chi tiêu mới
        binding.btnEditBudget.setOnClickListener {
            showEditBudgetDialog()
        }

        // Nút + THÊM THU
        binding.btnAddIncome.setOnClickListener {
            val intent = Intent(requireContext(), AddIncomeActivity::class.java)
            startActivity(intent)
        }

        // Nút - THÊM CHI
        binding.btnAddExpense.setOnClickListener {
            val intent = Intent(requireContext(), AddExpenseActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showEditBudgetDialog() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Ví dụ: 3000000"
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Thiết lập Hạn mức Chi tiêu tháng")
            .setMessage("Nhập hạn mức chi tiêu mong muốn (VNĐ):")
            .setView(input)
            .setPositiveButton("Lưu") { dialog, _ ->
                val limitStr = input.text.toString().trim()
                val limit = limitStr.toDoubleOrNull()
                if (limit != null && limit > 0) {
                    viewModel.setBudgetLimit(limit)
                } else {
                    Toast.makeText(requireContext(), "Hạn mức nhập vào không hợp lệ!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadHomeData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
