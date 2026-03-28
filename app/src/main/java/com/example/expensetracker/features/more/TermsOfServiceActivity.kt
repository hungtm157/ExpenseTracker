package com.example.expensetracker.features.more

import android.widget.ImageView
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity

/**
 * TermsOfServiceActivity — Màn hình hiển thị điều khoản dịch vụ.
 */
class TermsOfServiceActivity : BaseActivity(R.layout.activity_terms_of_service) {

    override fun initViews() {
        // Layout tĩnh hiển thị nội dung điều khoản
    }

    override fun initListeners() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}
