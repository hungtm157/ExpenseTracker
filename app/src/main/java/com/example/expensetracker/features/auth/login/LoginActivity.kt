package com.example.expensetracker.features.auth.login

import android.content.Intent
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.expensetracker.App
import com.example.expensetracker.R
import com.example.expensetracker.core.auth.GoogleSignInHelper
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.features.auth.forgotpassword.ForgotPasswordActivity
import com.example.expensetracker.features.auth.register.RegisterActivity
import com.example.expensetracker.features.home.HomeActivity

/**
 * LoginActivity — View cho màn hình Đăng nhập.
 * Chỉ xử lý UI; logic nghiệp vụ ủy quyền cho LoginController.
 */
class LoginActivity : BaseActivity(R.layout.activity_login), LoginListener, GoogleSignInHelper.GoogleSignInListener {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnLoginGoogle: Button
    private lateinit var ivTogglePassword: ImageView
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvGoToRegister: TextView
    private lateinit var progressBar: ProgressBar

    private val controller = LoginController(this)
    private lateinit var googleSignInHelper: GoogleSignInHelper
    private var isPasswordVisible = false
    private val TAG = "LoginActivity"



    override fun initViews() {
        val preferences = App.instance.preferences
        // Nếu đã đăng nhập trước đó → vào thẳng HomeActivity
        Log.d(TAG, "token: ${preferences.authToken}")
        if (preferences.isLoggedIn) {
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

        // Init Google Sign-In
        googleSignInHelper = GoogleSignInHelper(this, this)
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
            googleSignInHelper.launchSignIn()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GoogleSignInHelper.RC_GOOGLE_SIGN_IN) {
            googleSignInHelper.handleResult(data)
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

    // ─── GoogleSignInListener ────────────────────────────────────────────────

    override fun onGoogleSignInSuccess() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun onGoogleSignInFailure(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onGoogleSignInLoading(isLoading: Boolean) {
        if (isFinishing) return
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !isLoading
        btnLoginGoogle.isEnabled = !isLoading
    }
}
