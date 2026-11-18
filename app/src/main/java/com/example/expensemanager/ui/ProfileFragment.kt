package com.example.expensemanager.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.expensemanager.common.Resource
import com.example.expensemanager.data.local.SessionManager
import com.example.expensemanager.databinding.FragmentProfileBinding
import com.example.expensemanager.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

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

        sessionManager = SessionManager(requireContext())

        // Hiển thị thông tin người dùng từ phiên đăng nhập
        binding.tvUserName.text = sessionManager.getUserName()
        binding.tvUserEmail.text = sessionManager.getUserEmail()

        // Nút Xóa tất cả dữ liệu (Reset ứng dụng)
        binding.tvOptionClearData.setOnClickListener {
            showClearDataConfirmDialog()
        }

        // Nút đăng xuất -> Xóa phiên và về LoginActivity
        binding.tvOptionLogout.setOnClickListener {
            sessionManager.clearSession()
            Toast.makeText(requireContext(), "Đã đăng xuất!", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        observeViewModel()
    }

    private fun showClearDataConfirmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("⚠️ Cảnh báo xóa dữ liệu")
            .setMessage("Bạn có chắc chắn muốn xóa TOÀN BỘ khoản chi tiêu, thu nhập và reset lại số dư tài khoản về 0 không?\n\nHành động này không thể hoàn tác!")
            .setPositiveButton("XÓA TẤT CẢ") { _, _ ->
                viewModel.clearAllData()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.clearDataResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), resource.data, Toast.LENGTH_LONG).show()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
