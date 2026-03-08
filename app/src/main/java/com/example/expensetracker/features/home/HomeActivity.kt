package com.example.expensetracker.features.home

import android.os.Bundle
import android.widget.Toast
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * HomeActivity — Màn hình chính của ứng dụng.
 * Quản lý giao diện Tổng quan và Navigation.
 */
class HomeActivity : BaseActivity(R.layout.activity_home) {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var fab: FloatingActionButton

    override fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        fab = findViewById(R.id.fab)

        // Tắt animation shift của BottomNavigationView nếu cần
        bottomNavigation.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED
    }

    override fun initListeners() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Đã ở trang chủ
                    true
                }
                R.id.nav_statistics -> {
                    Toast.makeText(this, "Thống kê đang phát triển", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_plan -> {
                    Toast.makeText(this, "Kế hoạch đang phát triển", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_more -> {
                    Toast.makeText(this, "Khác đang phát triển", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        fab.setOnClickListener {
            Toast.makeText(this, "Mở màn hình Ghi giao dịch mới", Toast.LENGTH_SHORT).show()
            // TODO: Start AddTransactionActivity
        }
    }
}
