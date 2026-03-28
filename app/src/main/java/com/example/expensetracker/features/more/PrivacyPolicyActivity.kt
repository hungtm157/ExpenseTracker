package com.example.expensetracker.features.more

import android.widget.ImageView
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity

/**
 * PrivacyPolicyActivity — Màn hình hiển thị chính sách bảo mật và quyền truy cập.
 */
class PrivacyPolicyActivity : BaseActivity(R.layout.activity_privacy_policy) {

    override fun initViews() {
        // Layout tĩnh
    }

    override fun initListeners() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}
