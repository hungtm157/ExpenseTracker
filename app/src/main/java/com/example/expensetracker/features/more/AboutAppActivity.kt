package com.example.expensetracker.features.more

import android.widget.ImageView
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity

/**
 * AboutAppActivity — Màn hình giới thiệu về ứng dụng.
 */
class AboutAppActivity : BaseActivity(R.layout.activity_about_app) {

    override fun initViews() {
        // Layout tĩnh, chỉ cần nút back
    }

    override fun initListeners() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}
