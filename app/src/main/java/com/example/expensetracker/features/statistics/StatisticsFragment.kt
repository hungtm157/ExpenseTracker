package com.example.expensetracker.features.statistics

import android.view.View
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseFragment

/**
 * StatisticsFragment — Màn hình Thống kê (tab thứ hai trong Bottom Navigation).
 */
class StatisticsFragment : BaseFragment(R.layout.fragment_statistics) {

    override fun initViews(view: View) {
        // TODO: bind dữ liệu biểu đồ thống kê theo tuần/tháng/năm
    }

    override fun initListeners() {
        // TODO: xử lý sự kiện chọn tab tuần/tháng/năm
    }
}
