package com.example.expensetracker.features.home

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.features.more.MoreFragment
import com.example.expensetracker.features.plan.PlanFragment
import com.example.expensetracker.features.statistics.StatisticsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * HomeActivity — Container chính của ứng dụng.
 * Quản lý Bottom Navigation và swap Fragment theo từng tab.
 */
class HomeActivity : BaseActivity(R.layout.activity_home) {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var fab: FloatingActionButton

    override fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        fab = findViewById(R.id.fab)

        // Chỉ load fragment mặc định khi không có trạng thái được khôi phục
        // (tránh tạo lại fragment khi xoay màn hình)
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            switchFragment(HomeFragment())
            bottomNavigation.selectedItemId = R.id.nav_home
        }
    }

    override fun initListeners() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    switchFragment(HomeFragment())
                    true
                }
                R.id.nav_statistics -> {
                    switchFragment(StatisticsFragment())
                    true
                }
                R.id.nav_plan -> {
                    switchFragment(PlanFragment())
                    true
                }
                R.id.nav_more -> {
                    switchFragment(MoreFragment())
                    true
                }
                else -> false
            }
        }

        fab.setOnClickListener {
            Toast.makeText(this, "Ghi giao dịch mới", Toast.LENGTH_SHORT).show()
            // TODO: startActivity(Intent(this, AddTransactionActivity::class.java))
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
