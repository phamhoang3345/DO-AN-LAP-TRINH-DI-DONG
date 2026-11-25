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
import com.example.expensemanager.common.ImageLoader
import com.example.expensemanager.common.Resource
import com.example.expensemanager.data.local.SessionManager
import com.example.expensemanager.databinding.FragmentProfileBinding
import com.example.expensemanager.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()
    private var sessionManager: SessionManager? = null
    private var clearDialog: AlertDialog? = null

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

        val session = SessionManager(requireContext().applicationContext)
        sessionManager = session

        // Hiển thị thông tin người dùng từ phiên đăng nhập
        binding.tvUserName.text = session.getUserName()
        binding.tvUserEmail.text = session.getUserEmail()

        // Tải ảnh avatar bất đồng bộ bằng Glide
        ImageLoader.loadAvatar(binding.imgAvatar, url = null)

        // Nút Xóa tất cả dữ liệu (Reset ứng dụng)
        binding.tvOptionClearData.setOnClickListener {
            showClearDataConfirmDialog()
        }

        // Nút đăng xuất -> Xóa phiên và về LoginActivity
        binding.tvOptionLogout.setOnClickListener {
            session.clearSession()
            ImageLoader.clear(binding.imgAvatar)
            Toast.makeText(requireContext().applicationContext, "Đã đăng xuất!", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }

        observeViewModel()
    }

    private fun showClearDataConfirmDialog() {
        clearDialog?.dismiss()
        clearDialog = AlertDialog.Builder(requireContext())
            .setTitle("⚠️ Cảnh báo xóa dữ liệu")
            .setMessage(
                "Bạn có chắc chắn muốn xóa TOÀN BỘ khoản chi tiêu, thu nhập và reset lại số dư tài khoản về 0 không?\n\nHành động này không thể hoàn tác!"
            )
            .setPositiveButton("XÓA TẤT CẢ") { _, _ ->
                viewModel.clearAllData()
            }
            .setNegativeButton("Hủy", null)
            .create()
        clearDialog?.show()
    }

    private fun observeViewModel() {
        viewModel.clearDataResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(context?.applicationContext, resource.data, Toast.LENGTH_LONG).show()
                }
                is Resource.Error -> {
                    Toast.makeText(context?.applicationContext, resource.message, Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }
    }

    override fun onDestroyView() {
        clearDialog?.dismiss()
        clearDialog = null
        // Giải phóng Glide & ViewBinding tránh giữ tham chiếu View sau khi hủy Fragment
        ImageLoader.clear(binding.imgAvatar)
        super.onDestroyView()
        _binding = null
    }
}
