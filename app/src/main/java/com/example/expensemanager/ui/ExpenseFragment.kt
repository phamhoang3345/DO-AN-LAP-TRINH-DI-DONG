package com.example.expensemanager.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensemanager.adapter.ExpenseAdapter
import com.example.expensemanager.common.Resource
import com.example.expensemanager.databinding.FragmentExpenseBinding
import com.example.expensemanager.viewmodel.ExpenseViewModel

class ExpenseFragment : Fragment() {

    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var expenseAdapter: ExpenseAdapter

    private var _binding: FragmentExpenseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerExpenses.layoutManager = LinearLayoutManager(requireContext())
        expenseAdapter = ExpenseAdapter(emptyList())
        binding.recyclerExpenses.adapter = expenseAdapter

        // Bấm vào item để xem Màn hình Chi Tiết Khoản Chi (ExpenseDetailActivity)
        expenseAdapter.onItemClick = { expense ->
            val intent = Intent(requireContext(), ExpenseDetailActivity::class.java).apply {
                putExtra(ExpenseDetailActivity.EXTRA_EXPENSE_ID, expense.id)
                putExtra(ExpenseDetailActivity.EXTRA_EXPENSE_TITLE, expense.title)
                putExtra(ExpenseDetailActivity.EXTRA_EXPENSE_AMOUNT, expense.amount)
                putExtra(ExpenseDetailActivity.EXTRA_EXPENSE_DATE, expense.date)
                putExtra(ExpenseDetailActivity.EXTRA_EXPENSE_CATEGORY, expense.category)
            }
            startActivity(intent)
        }

        binding.btnRetry.setOnClickListener {
            viewModel.loadExpenses()
        }

        // Lắng nghe 3 trạng thái UI từ Resource State (Loading, Success, Error)
        viewModel.expensesState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerExpenses.visibility = View.GONE
                    binding.layoutError.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutError.visibility = View.GONE
                    binding.recyclerExpenses.visibility = View.VISIBLE
                    expenseAdapter.updateData(resource.data)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerExpenses.visibility = View.GONE
                    binding.layoutError.visibility = View.VISIBLE
                    binding.tvErrorMessage.text = resource.message
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding?.recyclerExpenses?.adapter = null
        super.onDestroyView()
        _binding = null
    }
}
