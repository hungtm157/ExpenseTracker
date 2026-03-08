package com.example.expensetracker.features.auth.forgotpassword

import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity

class ForgotPasswordActivity : BaseActivity(R.layout.activity_forgot_password), ForgotPasswordListener {

    private lateinit var ivBack: ImageView
    private lateinit var etEmail: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnSendOtp: Button

    private val controller = ForgotPasswordController(this)

    override fun initViews() {
        ivBack = findViewById(R.id.ivBack)
        etEmail = findViewById(R.id.etEmail)
        progressBar = findViewById(R.id.progressBar)
        btnSendOtp = findViewById(R.id.btnSendOtp)
    }

    override fun initListeners() {
        ivBack.setOnClickListener { finish() }

        btnSendOtp.setOnClickListener {
            val email = etEmail.text.toString().trim()
            controller.sendOtp(email)
        }
    }

    override fun onSendOtpSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        val intent = Intent(this, ForgotPasswordOtpActivity::class.java).apply {
            putExtra(ForgotPasswordOtpActivity.EXTRA_EMAIL, etEmail.text.toString().trim())
        }
        startActivity(intent)
    }

    override fun onSendOtpFailure(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onSendOtpLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnSendOtp.isEnabled = !isLoading
    }
}
