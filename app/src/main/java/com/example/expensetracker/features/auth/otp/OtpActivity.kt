package com.example.expensetracker.features.auth.otp

import android.content.Intent
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
import com.example.expensetracker.features.auth.login.LoginActivity

/**
 * OtpActivity — Màn xác thực OTP.
 * Nhận fullName, email, password từ RegisterActivity qua Intent extras.
 */
class OtpActivity : BaseActivity(R.layout.activity_otp), OtpListener {

    companion object {
        const val EXTRA_FULL_NAME = "extra_full_name"
        const val EXTRA_EMAIL = "extra_email"
        const val EXTRA_PASSWORD = "extra_password"
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

    private lateinit var fullName: String
    private lateinit var email: String
    private lateinit var password: String

    private val controller = OtpController(this)
    private var countDownTimer: CountDownTimer? = null

    override fun initViews() {
        fullName = intent.getStringExtra(EXTRA_FULL_NAME) ?: ""
        email = intent.getStringExtra(EXTRA_EMAIL) ?: ""
        password = intent.getStringExtra(EXTRA_PASSWORD) ?: ""

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

        // Auto-focus: khi nhập xong 1 ô → focus sang ô kế
        setupOtpBoxes()

        btnVerify.setOnClickListener {
            val otp = getOtpCode()
            controller.verifyOtp(fullName, email, password, otp)
        }

        tvResendOtp.setOnClickListener {
            Toast.makeText(this, "Vui lòng liên hệ hỗ trợ qua email", Toast.LENGTH_SHORT).show()
        }
    }

    /** Lấy mã OTP từ 6 ô edit text */
    private fun getOtpCode(): String =
        "${etOtp1.text}${etOtp2.text}${etOtp3.text}${etOtp4.text}${etOtp5.text}${etOtp6.text}"

    /** Đếm ngược 60 giây */
    private fun startCountdown() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(60_000, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = millisUntilFinished / 1000
                tvCountdown.text = "0:%02d".format(sec)
            }

            override fun onFinish() {
                tvCountdown.text = "0:00"
            }
        }.start()
    }

    /** Tự động chuyển focus khi nhập đủ 1 chữ số, hoặc lùi khi xóa */
    private fun setupOtpBoxes() {
        val boxes = listOf(etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6)

        boxes.forEachIndexed { index, box ->
            box.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        // Chuyển sang ô tiếp theo
                        if (index < boxes.size - 1) boxes[index + 1].requestFocus()
                    } else if (s?.isEmpty() == true) {
                        // Lùi về ô trước
                        if (index > 0) boxes[index - 1].requestFocus()
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    // ─── OtpListener ─────────────────────────────────────────────────────────

    override fun onVerifySuccess() {
        Toast.makeText(this, "Đăng ký thành công! Hãy đăng nhập.", Toast.LENGTH_LONG).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onVerifyFailure(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onVerifyLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnVerify.isEnabled = !isLoading
    }
}
