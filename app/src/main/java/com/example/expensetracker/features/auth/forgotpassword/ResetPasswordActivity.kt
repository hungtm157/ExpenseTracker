package com.example.expensetracker.features.auth.forgotpassword

import android.content.Intent
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.features.auth.login.LoginActivity

class ResetPasswordActivity : BaseActivity(R.layout.activity_reset_password), ResetPasswordListener {

    companion object {
        const val EXTRA_TOKEN = "extra_token"
    }

    private lateinit var ivBack: ImageView
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var ivToggleNewPassword: ImageView
    private lateinit var ivToggleConfirmPassword: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnResetPassword: Button

    private lateinit var token: String
    private val controller = ResetPasswordController(this)
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun initViews() {
        token = intent.getStringExtra(EXTRA_TOKEN) ?: ""

        ivBack = findViewById(R.id.ivBack)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        ivToggleNewPassword = findViewById(R.id.ivToggleNewPassword)
        ivToggleConfirmPassword = findViewById(R.id.ivToggleConfirmPassword)
        progressBar = findViewById(R.id.progressBar)
        btnResetPassword = findViewById(R.id.btnResetPassword)
    }

    override fun initListeners() {
        ivBack.setOnClickListener { finish() }

        ivToggleNewPassword.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            etNewPassword.transformationMethod =
                if (isNewPasswordVisible) null else PasswordTransformationMethod.getInstance()
            etNewPassword.setSelection(etNewPassword.text.length)
        }

        ivToggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            etConfirmPassword.transformationMethod =
                if (isConfirmPasswordVisible) null else PasswordTransformationMethod.getInstance()
            etConfirmPassword.setSelection(etConfirmPassword.text.length)
        }

        btnResetPassword.setOnClickListener {
            val password = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            controller.resetPassword(token, password, confirmPassword)
        }
    }

    override fun onResetSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onResetFailure(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onResetLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnResetPassword.isEnabled = !isLoading
    }
}
