package com.example.expensetracker.features.profile

import android.content.Intent
import android.widget.Button
import android.widget.TextView
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.data.local.AppPreferences
import com.example.expensetracker.features.auth.login.LoginActivity
import com.example.expensetracker.features.profile.ProfileController.ProfileListener

/**
 * ProfileActivity — View cho màn hình Cá nhân.
 */
class ProfileActivity : BaseActivity(R.layout.activity_profile), ProfileListener {

    private lateinit var tvUserName : TextView
    private lateinit var btnLogout  : Button
    private lateinit var controller : ProfileController

    override fun initViews() {
        tvUserName = findViewById(R.id.tvUserName)
        btnLogout  = findViewById(R.id.btnLogout)

        val prefs = AppPreferences(this)
        controller = ProfileController(prefs, this)
    }

    override fun initObservers() {
        controller.loadProfile()
    }

    override fun initListeners() {
        btnLogout.setOnClickListener { controller.logout() }
    }

    // ─── ProfileListener ─────────────────────────────────────────────────────

    override fun onProfileLoaded(name: String) {
        tvUserName.text = name
    }

    override fun onLoggedOut() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}
