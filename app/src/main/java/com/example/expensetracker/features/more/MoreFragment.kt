package com.example.expensetracker.features.more

import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseFragment
import com.example.expensetracker.data.local.AppPreferences
import com.example.expensetracker.features.auth.login.LoginActivity
import com.example.expensetracker.features.category.CategoryActivity

/**
 * MoreFragment — Màn hình Cài đặt (tab Khác trong Bottom Navigation).
 * Hiển thị: thông tin user, ngân sách tháng, tổng quan tài chính, menu cài đặt.
 */
class MoreFragment : BaseFragment(R.layout.fragment_more) {

    private lateinit var menuProfile: LinearLayout
    private lateinit var menuCategories: LinearLayout
    private lateinit var menuWallet: LinearLayout
    private lateinit var menuNotifications: LinearLayout
    private lateinit var menuSecurity: LinearLayout
    private lateinit var menuAbout: LinearLayout
    private lateinit var btnLogout: LinearLayout
    private lateinit var tvEditBudget: TextView

    override fun initViews(view: View) {
        menuProfile = view.findViewById(R.id.menuProfile)
        menuCategories = view.findViewById(R.id.menuCategories)
        menuWallet = view.findViewById(R.id.menuWallet)
        menuNotifications = view.findViewById(R.id.menuNotifications)
        menuSecurity = view.findViewById(R.id.menuSecurity)
        menuAbout = view.findViewById(R.id.menuAbout)
        btnLogout = view.findViewById(R.id.btnLogout)
        tvEditBudget = view.findViewById(R.id.tvEditBudget)
    }

    override fun initListeners() {
        menuProfile.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.expensetracker.features.profile.ProfileActivity::class.java))
        }
        menuCategories.setOnClickListener {
            startActivity(Intent(requireContext(), CategoryActivity::class.java))
        }
        menuWallet.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.expensetracker.features.wallet.WalletActivity::class.java))
        }
        menuNotifications.setOnClickListener {
            Toast.makeText(requireContext(), "Thông báo", Toast.LENGTH_SHORT).show()
        }
        menuSecurity.setOnClickListener {
            Toast.makeText(requireContext(), "Bảo mật", Toast.LENGTH_SHORT).show()
        }
        menuAbout.setOnClickListener {
            Toast.makeText(requireContext(), "Về ứng dụng", Toast.LENGTH_SHORT).show()
        }
        tvEditBudget.setOnClickListener {
            Toast.makeText(requireContext(), "Chỉnh sửa ngân sách", Toast.LENGTH_SHORT).show()
        }
        btnLogout.setOnClickListener {
            // Xoá toàn bộ: authToken, userId, userName, isLoggedIn
            AppPreferences(requireContext()).clear()

            // Về LoginActivity và xoá toàn bộ back stack
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}
