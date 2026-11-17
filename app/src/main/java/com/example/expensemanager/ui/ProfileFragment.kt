package com.example.expensemanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.expensemanager.databinding.FragmentProfileBinding
import com.example.expensemanager.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            binding.tvUserName.text = profile.name
            binding.tvUserEmail.text = profile.email
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
