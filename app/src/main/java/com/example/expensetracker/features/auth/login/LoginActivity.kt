package com.example.expensetracker.features.auth.login

import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.features.home.HomeActivity

/**
 * LoginActivity — View cho màn hình Đăng nhập.
 * Chỉ xử lý UI; logic nghiệp vụ ủy quyền cho LoginController.
 */
class LoginActivity : BaseActivity(R.layout.activity_login), LoginListener {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar

    private val controller = LoginController(this)

    override fun initViews() {
        etEmail     = findViewById(R.id.etEmail)
        etPassword  = findViewById(R.id.etPassword)
        btnLogin    = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)
    }

    override fun initListeners() {
        btnLogin.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            controller.login(email, password)
        }
    }

    // ─── LoginListener ───────────────────────────────────────────────────────

    override fun onLoginSuccess() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun onLoginFailure(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onLoginLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnLogin.isEnabled     = !isLoading
    }
}
