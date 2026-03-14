package com.example.expensetracker.features.home

import android.view.View
import android.widget.Toast
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseFragment
import com.example.expensetracker.features.transaction.TransactionHistoryActivity

/**
 * HomeFragment — Màn hình Tổng quan (tab đầu tiên trong Bottom Navigation).
 */
class HomeFragment : BaseFragment(R.layout.fragment_home) {

    override fun initViews(view: View) {
        // TODO: bind dữ liệu thật từ Controller/API
        val btnViewAllTransactions = view.findViewById<android.widget.TextView>(R.id.btnViewAllTransactions)
        btnViewAllTransactions.setOnClickListener {
            startActivity(android.content.Intent(requireContext(), TransactionHistoryActivity::class.java))
        }

        val btnAddTransactionCenter = view.findViewById<android.widget.Button>(R.id.btnAddTransactionCenter)
        btnAddTransactionCenter.setOnClickListener {
            startActivity(android.content.Intent(requireContext(), com.example.expensetracker.features.transaction.AddTransactionActivity::class.java))
        }
    }

    override fun initListeners() {
        // TODO: xử lý sự kiện chip lọc, xem chi tiết danh mục
    }
}
