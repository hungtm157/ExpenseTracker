package com.example.expensetracker.features.auth.login

import android.content.Intent
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.expensetracker.App
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.features.auth.forgotpassword.ForgotPasswordActivity
import com.example.expensetracker.features.auth.register.RegisterActivity
import com.example.expensetracker.features.home.HomeActivity

/**
 * LoginActivity — View cho màn hình Đăng nhập.
 * Chỉ xử lý UI; logic nghiệp vụ ủy quyền cho LoginController.
 */
class LoginActivity : BaseActivity(R.layout.activity_login), LoginListener {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnLoginGoogle: Button
    private lateinit var ivTogglePassword: ImageView
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvGoToRegister: TextView
    private lateinit var progressBar: ProgressBar

    private val controller = LoginController(this)
    private var isPasswordVisible = false

    override fun initViews() {
        // Nếu đã đăng nhập trước đó → vào thẳng HomeActivity
        if (App.instance.preferences.isLoggedIn) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnLoginGoogle = findViewById(R.id.btnLoginGoogle)
        ivTogglePassword = findViewById(R.id.ivTogglePassword)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvGoToRegister = findViewById(R.id.tvGoToRegister)
        progressBar = findViewById(R.id.progressBar)
    }

    override fun initListeners() {
        if (isFinishing) return

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            controller.login(email, password)
        }

        ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            etPassword.transformationMethod =
                if (isPasswordVisible) null else PasswordTransformationMethod.getInstance()
            etPassword.setSelection(etPassword.text.length)
        }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnLoginGoogle.setOnClickListener {
            Toast.makeText(this, "Đăng nhập với Google đang phát triển", Toast.LENGTH_SHORT).show()
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
        if (isFinishing) return
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !isLoading
    }
}
