package com.example.expensetracker.features.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.features.more.MoreFragment
import com.example.expensetracker.features.plan.PlanFragment
import com.example.expensetracker.features.statistics.StatisticsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.expensetracker.features.transaction.AddTransactionActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.expensetracker.utils.AdManager

/**
 * HomeActivity — Container chính của ứng dụng.
 * Quản lý Bottom Navigation và swap Fragment theo từng tab.
 */
class HomeActivity : BaseActivity(R.layout.activity_home) {

    override val handleBottomInsets = false

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
            
            // Show ad on app entry
            AdManager.showInterstitialAd(this) {}
        }

        // Xử lý nới rộng padding cho fragmentContainer và BottomAppBar khi Edge-to-Edge
        val fragmentContainer = findViewById<android.view.View>(R.id.fragmentContainer)
        val initialPaddingBottom = fragmentContainer.paddingBottom

        val bottomAppBar = findViewById<com.google.android.material.bottomappbar.BottomAppBar>(R.id.bottomAppBar)
        val initialAppBarHeight = bottomAppBar.layoutParams.height

        ViewCompat.setOnApplyWindowInsetsListener(fragmentContainer) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Tăng padding dưới của fragmentContainer để nội dung không bị che
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                initialPaddingBottom + systemBars.bottom
            )
            
            // Xử lý BottomAppBar: tăng padding và chiều cao lên tương ứng systemBars.bottom
            bottomAppBar.setPadding(
                bottomAppBar.paddingLeft,
                bottomAppBar.paddingTop,
                bottomAppBar.paddingRight,
                systemBars.bottom
            )
            val params = bottomAppBar.layoutParams
            params.height = initialAppBarHeight + systemBars.bottom
            bottomAppBar.layoutParams = params
            
            insets
        }

        // Yêu cầu quyền thông báo cho Android 13+
        requestNotificationPermission()
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
            AdManager.showInterstitialAd(this) {
                val intent = Intent(this, AddTransactionActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /**
     * Android 13 (API 33+) yêu cầu quyền POST_NOTIFICATIONS tại runtime.
     * Nếu chưa được cấp → hiện dialog xin quyền.
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }
}
