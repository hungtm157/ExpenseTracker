package com.example.expensetracker.features.auth.register

import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity

/**
 * RegisterActivity — View cho màn hình Đăng ký.
 * Logic nghiệp vụ sẽ được thêm vào RegisterController sau.
 */
class RegisterActivity : BaseActivity(R.layout.activity_register) {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button

    override fun initViews() {
        etName      = findViewById(R.id.etName)
        etEmail     = findViewById(R.id.etEmail)
        etPassword  = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
    }

    override fun initListeners() {
        btnRegister.setOnClickListener {
            // TODO: Thêm RegisterController và xử lý đăng ký
            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
        }
    }
}
