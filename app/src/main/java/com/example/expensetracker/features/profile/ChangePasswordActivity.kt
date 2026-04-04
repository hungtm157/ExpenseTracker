package com.example.expensetracker.features.profile

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.repository.AuthRepository

/**
 * ChangePasswordActivity — Màn hình Đổi mật khẩu.
 */
class ChangePasswordActivity : BaseActivity(R.layout.activity_change_password), ChangePasswordListener {

    private lateinit var btnBack: ImageView
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var ivToggleCurrent: ImageView
    private lateinit var ivToggleNew: ImageView
    private lateinit var ivToggleConfirm: ImageView
    private lateinit var btnChangePassword: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var controller: ChangePasswordController

    private var isCurrentVisible = false
    private var isNewVisible = false
    private var isConfirmVisible = false

    override fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        ivToggleCurrent = findViewById(R.id.ivToggleCurrentPassword)
        ivToggleNew = findViewById(R.id.ivToggleNewPassword)
        ivToggleConfirm = findViewById(R.id.ivToggleConfirmPassword)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        progressBar = findViewById(R.id.progressBar)

        val apiService = ApiClient.create(ApiService::class.java)
        val repository = AuthRepository(apiService)
        controller = ChangePasswordController(repository, this)
    }

    override fun initListeners() {
        btnBack.setOnClickListener { finish() }

        ivToggleCurrent.setOnClickListener {
            isCurrentVisible = !isCurrentVisible
            togglePasswordVisibility(etCurrentPassword, ivToggleCurrent, isCurrentVisible)
        }

        ivToggleNew.setOnClickListener {
            isNewVisible = !isNewVisible
            togglePasswordVisibility(etNewPassword, ivToggleNew, isNewVisible)
        }

        ivToggleConfirm.setOnClickListener {
            isConfirmVisible = !isConfirmVisible
            togglePasswordVisibility(etConfirmPassword, ivToggleConfirm, isConfirmVisible)
        }

        btnChangePassword.setOnClickListener {
            val current = etCurrentPassword.text.toString().trim()
            val new = etNewPassword.text.toString().trim()
            val confirm = etConfirmPassword.text.toString().trim()
            
            // Clear prior errors
            etCurrentPassword.error = null
            etNewPassword.error = null
            etConfirmPassword.error = null
            
            controller.changePassword(current, new, confirm)
        }
    }

    private fun togglePasswordVisibility(editText: EditText, imageView: ImageView, isVisible: Boolean) {
        if (isVisible) {
            editText.transformationMethod = null
            imageView.setImageResource(R.drawable.ic_eye_hide) // Replace with your actual eye icons if different
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            imageView.setImageResource(R.drawable.ic_eye_show)
        }
        editText.setSelection(editText.text.length)
    }

    // --- ChangePasswordListener Callbacks ---

    override fun onLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnChangePassword.isEnabled = !isLoading
    }

    override fun onSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onFieldError(currentError: String?, newError: String?, confirmError: String?) {
        currentError?.let { etCurrentPassword.error = it }
        newError?.let { etNewPassword.error = it }
        confirmError?.let { etConfirmPassword.error = it }
    }
}
