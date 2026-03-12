package com.example.expensetracker.features.more

import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseFragment
import com.example.expensetracker.features.auth.login.LoginActivity

/**
 * MoreFragment — Màn hình Khác: thông tin tài khoản, cài đặt, đăng xuất.
 */
class MoreFragment : BaseFragment(R.layout.fragment_more) {

    private lateinit var menuProfile: LinearLayout
    private lateinit var menuWallet: LinearLayout
    private lateinit var menuSettings: LinearLayout
    private lateinit var menuLanguage: LinearLayout
    private lateinit var btnLogout: androidx.appcompat.widget.AppCompatButton

    override fun initViews(view: View) {
        menuProfile = view.findViewById(R.id.menuProfile)
        menuWallet = view.findViewById(R.id.menuWallet)
        menuSettings = view.findViewById(R.id.menuSettings)
        menuLanguage = view.findViewById(R.id.menuLanguage)
        btnLogout = view.findViewById(R.id.btnLogout)
    }

    override fun initListeners() {
        menuProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Thông tin cá nhân", Toast.LENGTH_SHORT).show()
        }
        menuWallet.setOnClickListener {
            Toast.makeText(requireContext(), "Tài khoản ví", Toast.LENGTH_SHORT).show()
        }
        menuSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Thiết lập", Toast.LENGTH_SHORT).show()
        }
        menuLanguage.setOnClickListener {
            Toast.makeText(requireContext(), "Ngôn ngữ", Toast.LENGTH_SHORT).show()
        }
        btnLogout.setOnClickListener {
            // TODO: xóa token, quay về màn Login
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
