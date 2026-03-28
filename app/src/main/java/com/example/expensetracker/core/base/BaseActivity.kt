package com.example.expensetracker.core.base

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.expensetracker.R

/**
 * BaseActivity — lớp nền cho mọi Activity trong ứng dụng.
 * Xử lý các tác vụ chung: gắn layout, edge-to-edge, v.v.
 */
abstract class BaseActivity(@LayoutRes private val layoutResId: Int) : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        super.onCreate(savedInstanceState)
        
        // Kích hoạt hiển thị tràn viền (Edge-to-Edge)
        enableEdgeToEdge()
        
        // Cấu hình trạng thái thanh hệ thống: Trong suốt và chữ trắng
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        setContentView(layoutResId)

        // Tự động xử lý dải màu gradient cho phần Status Bar (notch) và insets
        setupStatusBarGradient()

        initViews()
        initListeners()
        initObservers()
    }

    /**
     * Tự động thêm một View có nền gradient vào phần thanh trạng thái (status bar).
     * Và xử lý padding cho nội dung phía dưới để không bị đè bởi Notch.
     */
    private fun setupStatusBarGradient() {
        val rootContentView = findViewById<ViewGroup>(android.R.id.content)
        
        // Tạo view nền gradient cho status bar
        val statusBarBg = View(this).apply {
            setBackgroundResource(R.drawable.bg_gradient_green)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0
            )
        }
        
        // Thêm miếng gradient vào đầu rootContentView
        rootContentView.addView(statusBarBg)

        ViewCompat.setOnApplyWindowInsetsListener(rootContentView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // 1. Cập nhật chiều cao miếng gradient
            statusBarBg.layoutParams = (statusBarBg.layoutParams as FrameLayout.LayoutParams).apply {
                height = systemBars.top
            }
            
            // 2. Padding cho các View khác (nội dung ứng dụng) để không bị đè bởi Notch
            // Chúng ta không pad rootContentView vì nó sẽ đẩy cả miếng gradient xuống.
            // Thay vào đó, ta pad các "con" khác của nó.
            for (i in 0 until rootContentView.childCount) {
                val child = rootContentView.getChildAt(i)
                if (child != statusBarBg) {
                    // Áp dụng padding hệ thống để tránh Notch/Nav bar
                    child.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                }
            }
            
            insets
        }
    }

    /** Khởi tạo và ánh xạ View (findViewById, ViewBinding, v.v.) */
    protected open fun initViews() {}

    /** Gán sự kiện click, touch, v.v. */
    protected open fun initListeners() {}

    /** Lắng nghe dữ liệu / trạng thái từ Controller */
    protected open fun initObservers() {}
}
