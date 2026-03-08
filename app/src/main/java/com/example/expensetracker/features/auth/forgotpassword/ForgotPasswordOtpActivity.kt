package com.example.expensetracker.features.auth.forgotpassword

import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity

import android.content.Intent

class ForgotPasswordOtpActivity : BaseActivity(R.layout.activity_otp), ForgotPasswordOtpListener {

    companion object {
        const val EXTRA_EMAIL = "extra_email"
    }

    private lateinit var ivBack: ImageView
    private lateinit var tvEmailHint: TextView
    private lateinit var etOtp1: EditText
    private lateinit var etOtp2: EditText
    private lateinit var etOtp3: EditText
    private lateinit var etOtp4: EditText
    private lateinit var etOtp5: EditText
    private lateinit var etOtp6: EditText
    private lateinit var tvCountdown: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnVerify: Button
    private lateinit var tvResendOtp: TextView

    private lateinit var email: String
    private val controller = ForgotPasswordOtpController(this)
    private var countDownTimer: CountDownTimer? = null
    private var canResend = false

    override fun initViews() {
        email = intent.getStringExtra(EXTRA_EMAIL) ?: ""

        ivBack = findViewById(R.id.ivBack)
        tvEmailHint = findViewById(R.id.tvEmailHint)
        etOtp1 = findViewById(R.id.etOtp1)
        etOtp2 = findViewById(R.id.etOtp2)
        etOtp3 = findViewById(R.id.etOtp3)
        etOtp4 = findViewById(R.id.etOtp4)
        etOtp5 = findViewById(R.id.etOtp5)
        etOtp6 = findViewById(R.id.etOtp6)
        tvCountdown = findViewById(R.id.tvCountdown)
        progressBar = findViewById(R.id.progressBar)
        btnVerify = findViewById(R.id.btnVerify)
        tvResendOtp = findViewById(R.id.tvResendOtp)

        tvEmailHint.text = email
        startCountdown()
    }

    override fun initListeners() {
        ivBack.setOnClickListener { finish() }

        setupOtpBoxes()

        btnVerify.setOnClickListener {
            controller.verifyOtp(email, getOtpCode())
        }

        tvResendOtp.setOnClickListener {
            if (canResend) {
                controller.resendOtp(email)
                canResend = false
                startCountdown()
            } else {
                Toast.makeText(this, "Vui lòng chờ hết thời gian đếm ngược", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getOtpCode(): String =
        "${etOtp1.text}${etOtp2.text}${etOtp3.text}${etOtp4.text}${etOtp5.text}${etOtp6.text}"

    private fun startCountdown() {
        countDownTimer?.cancel()
        canResend = false
        countDownTimer = object : CountDownTimer(60_000, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = millisUntilFinished / 1000
                tvCountdown.text = "0:%02d".format(sec)
            }
            override fun onFinish() {
                tvCountdown.text = "0:00"
                canResend = true
            }
        }.start()
    }

    private fun setupOtpBoxes() {
        val boxes = listOf(etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6)
        boxes.forEachIndexed { index, box ->
            box.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && index < boxes.size - 1) boxes[index + 1].requestFocus()
                    else if (s?.isEmpty() == true && index > 0) boxes[index - 1].requestFocus()
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    override fun onVerifySuccess(message: String, token: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        val intent = Intent(this, ResetPasswordActivity::class.java).apply {
            putExtra(ResetPasswordActivity.EXTRA_TOKEN, token)
        }
        startActivity(intent)
        finish()
    }

    override fun onVerifyFailure(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onVerifyLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnVerify.isEnabled = !isLoading
    }

    override fun onResendSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResendFailure(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}
