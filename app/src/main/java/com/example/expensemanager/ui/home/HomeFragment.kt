package com.example.expensemanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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

        viewModel.greeting.observe(viewLifecycleOwner) { text ->
            binding.tvGreeting.text = text
        }

        viewModel.currentBalance.observe(viewLifecycleOwner) { amount ->
            binding.tvBalanceAmount.text = amount
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
