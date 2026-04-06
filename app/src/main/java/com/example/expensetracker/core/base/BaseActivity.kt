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
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.example.expensetracker.R

/**
 * BaseActivity — lớp nền cho mọi Activity trong ứng dụng.
 * Xử lý các tác vụ chung: gắn layout, edge-to-edge, v.v.
 */
abstract class BaseActivity(@LayoutRes private val layoutResId: Int) : AppCompatActivity() {

    /**
     * Cờ cho phép tự động xử lý khoảng trống dưới cùng (thanh điều hướng).
     * Mặc định là true. Các màn hình như HomeActivity có thể override thành false
     * để tự xử lý edge-to-edge cho BottomAppBar.
     */
    protected open val handleBottomInsets: Boolean = true

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
            statusBarBg.updateLayoutParams<FrameLayout.LayoutParams> {
                height = systemBars.top
            }

            // 2. Padding cho các View khác (nội dung ứng dụng) để không bị đè bởi Notch
            for (i in 0 until rootContentView.childCount) {
                val child = rootContentView.getChildAt(i)
                if (child != statusBarBg) {
                    child.updatePadding(
                        left = systemBars.left,
                        top = systemBars.top,
                        right = systemBars.right,
                        bottom = if (handleBottomInsets) systemBars.bottom else 0
                    )
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
