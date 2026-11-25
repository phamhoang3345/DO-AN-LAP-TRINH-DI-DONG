package com.example.expensemanager.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.expensemanager.common.ImageLoader
import com.example.expensemanager.common.Resource
import com.example.expensemanager.data.local.SessionManager
import com.example.expensemanager.databinding.FragmentProfileBinding
import com.example.expensemanager.viewmodel.ProfileViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

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

        // Nút Chỉnh sửa hồ sơ
        binding.tvOptionSettings.setOnClickListener {
            showEditProfileDialog()
        }

        // Nút Đổi mật khẩu
        binding.tvOptionPassword.setOnClickListener {
            showChangePasswordDialog()
        }

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

    private fun showEditProfileDialog() {
        val context = requireContext()
        val session = sessionManager ?: return

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }

        val etName = EditText(context).apply {
            hint = "Họ và tên"
            setText(session.getUserName())
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        }

        val etEmail = EditText(context).apply {
            hint = "Email"
            setText(session.getUserEmail())
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

        layout.addView(etName)
        layout.addView(etEmail)

        AlertDialog.Builder(context)
            .setTitle("Chỉnh sửa hồ sơ")
            .setView(layout)
            .setPositiveButton("Lưu") { dialog, _ ->
                val name = etName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                if (name.isNotEmpty() && email.isNotEmpty()) {
                    session.saveSession(true, email, name)
                    binding.tvUserName.text = name
                    binding.tvUserEmail.text = email
                    Toast.makeText(context, "Đã cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Vui lòng điền đầy đủ tên và email!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }

        val etOldPass = EditText(context).apply {
            hint = "Mật khẩu hiện tại"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val etNewPass = EditText(context).apply {
            hint = "Mật khẩu mới (ít nhất 8 ký tự)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val etConfirmPass = EditText(context).apply {
            hint = "Xác nhận mật khẩu mới"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        layout.addView(etOldPass)
        layout.addView(etNewPass)
        layout.addView(etConfirmPass)

        AlertDialog.Builder(context)
            .setTitle("Đổi mật khẩu")
            .setView(layout)
            .setPositiveButton("Đổi mật khẩu") { dialog, _ ->
                val oldPass = etOldPass.text.toString().trim()
                val newPass = etNewPass.text.toString().trim()
                val confirmPass = etConfirmPass.text.toString().trim()

                if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newPass.length < 8) {
                    Toast.makeText(context, "Mật khẩu mới phải có ít nhất 8 ký tự!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newPass != confirmPass) {
                    Toast.makeText(context, "Mật khẩu mới xác nhận không khớp!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val user = FirebaseAuth.getInstance().currentUser
                if (user != null && user.email != null) {
                    val credential = EmailAuthProvider.getCredential(user.email!!, oldPass)
                    user.reauthenticate(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            user.updatePassword(newPass).addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(context, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                } else {
                                    Toast.makeText(context, "Lỗi: ${updateTask.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Mật khẩu hiện tại không chính xác!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
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
