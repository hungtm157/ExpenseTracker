package com.example.expensetracker.features.auth.register

import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.expensetracker.R
import com.example.expensetracker.core.auth.GoogleSignInHelper
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.features.auth.login.LoginActivity
import com.example.expensetracker.features.auth.otp.OtpActivity
import com.example.expensetracker.features.home.HomeActivity

class RegisterActivity : BaseActivity(R.layout.activity_register), RegisterListener, GoogleSignInHelper.GoogleSignInListener {

    private lateinit var ivBack: ImageView
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var ivTogglePassword: ImageView
    private lateinit var ivToggleConfirmPassword: ImageView
    private lateinit var cbTerms: CheckBox
    private lateinit var tvTermsText: TextView
    private lateinit var btnRegister: Button
    private lateinit var btnRegisterGoogle: Button
    private lateinit var tvGoToLogin: TextView

    private val controller = RegisterController(this)
    private lateinit var googleSignInHelper: GoogleSignInHelper
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun initViews() {
        ivBack = findViewById(R.id.ivBack)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        ivTogglePassword = findViewById(R.id.ivTogglePassword)
        ivToggleConfirmPassword = findViewById(R.id.ivToggleConfirmPassword)
        cbTerms = findViewById(R.id.cbTerms)
        tvTermsText = findViewById(R.id.tvTermsText)
        btnRegister = findViewById(R.id.btnRegister)
        btnRegisterGoogle = findViewById(R.id.btnRegisterGoogle)
        tvGoToLogin = findViewById(R.id.tvGoToLogin)

        setupTermsText()

        // Init Google Sign-In
        googleSignInHelper = GoogleSignInHelper(this, this)
    }

    private fun setupTermsText() {
        val green = ContextCompat.getColor(this, R.color.green_mid)
        val fullText = "Tôi đồng ý với Điều khoản dịch vụ và Chính sách bảo mật"
        val spannable = SpannableString(fullText)

        val termsStart = fullText.indexOf("Điều khoản dịch vụ")
        val termsEnd = termsStart + "Điều khoản dịch vụ".length
        spannable.setSpan(ForegroundColorSpan(green), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@RegisterActivity, "Điều khoản dịch vụ đang cập nhật", Toast.LENGTH_SHORT).show()
            }
            override fun updateDrawState(ds: android.text.TextPaint) {
                ds.color = green
                ds.isUnderlineText = false
            }
        }, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val privacyStart = fullText.indexOf("Chính sách bảo mật")
        val privacyEnd = privacyStart + "Chính sách bảo mật".length
        spannable.setSpan(ForegroundColorSpan(green), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@RegisterActivity, "Chính sách bảo mật đang cập nhật", Toast.LENGTH_SHORT).show()
            }
            override fun updateDrawState(ds: android.text.TextPaint) {
                ds.color = green
                ds.isUnderlineText = false
            }
        }, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvTermsText.text = spannable
        tvTermsText.movementMethod = LinkMovementMethod.getInstance()
        tvTermsText.highlightColor = android.graphics.Color.TRANSPARENT
    }

    override fun initListeners() {
        ivBack.setOnClickListener { finish() }

        ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            etPassword.transformationMethod =
                if (isPasswordVisible) null else PasswordTransformationMethod.getInstance()
            etPassword.setSelection(etPassword.text.length)
        }

        ivToggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            etConfirmPassword.transformationMethod =
                if (isConfirmPasswordVisible) null else PasswordTransformationMethod.getInstance()
            etConfirmPassword.setSelection(etConfirmPassword.text.length)
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            if (!cbTerms.isChecked) {
                Toast.makeText(this, "Vui lòng đồng ý với điều khoản dịch vụ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            controller.register(name, email, password, confirmPassword)
        }

        btnRegisterGoogle.setOnClickListener {
            googleSignInHelper.launchSignIn()
        }

        tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GoogleSignInHelper.RC_GOOGLE_SIGN_IN) {
            googleSignInHelper.handleResult(data)
        }
    }

    // ─── RegisterListener ────────────────────────────────────────────────────

    override fun onRegisterSuccess() {
        val intent = Intent(this, OtpActivity::class.java).apply {
            putExtra(OtpActivity.EXTRA_FULL_NAME, etName.text.toString().trim())
            putExtra(OtpActivity.EXTRA_EMAIL, etEmail.text.toString().trim())
            putExtra(OtpActivity.EXTRA_PASSWORD, etPassword.text.toString().trim())
        }
        startActivity(intent)
    }

    override fun onRegisterFailure(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onRegisterLoading(isLoading: Boolean) {
        btnRegister.isEnabled = !isLoading
    }

    // ─── GoogleSignInListener ────────────────────────────────────────────────

    override fun onGoogleSignInSuccess() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onGoogleSignInFailure(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onGoogleSignInLoading(isLoading: Boolean) {
        btnRegister.isEnabled = !isLoading
        btnRegisterGoogle.isEnabled = !isLoading
    }
}
