package com.example.expensetracker.core.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity

/**
 * BaseActivity — lớp nền cho mọi Activity trong ứng dụng.
 * Xử lý các tác vụ chung: gắn layout, edge-to-edge, v.v.
 */
abstract class BaseActivity(@LayoutRes private val layoutResId: Int) : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)
        initViews()
        initListeners()
        initObservers()
    }

    /** Khởi tạo và ánh xạ View (findViewById, ViewBinding, v.v.) */
    protected open fun initViews() {}

    /** Gán sự kiện click, touch, v.v. */
    protected open fun initListeners() {}

    /** Lắng nghe dữ liệu / trạng thái từ Controller */
    protected open fun initObservers() {}
}
