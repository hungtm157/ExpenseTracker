package com.example.expensetracker.features.home

import android.view.View
import android.widget.Toast
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseFragment

/**
 * HomeFragment — Màn hình Tổng quan (tab đầu tiên trong Bottom Navigation).
 */
class HomeFragment : BaseFragment(R.layout.fragment_home) {

    override fun initViews(view: View) {
        // TODO: bind dữ liệu thật từ Controller/API
    }

    override fun initListeners() {
        // TODO: xử lý sự kiện chip lọc, xem chi tiết danh mục
    }
}
