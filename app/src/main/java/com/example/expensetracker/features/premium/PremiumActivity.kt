package com.example.expensetracker.features.premium

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.local.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

/**
 * PremiumActivity — Màn hình nâng cấp tài khoản Premium.
 */
class PremiumActivity : BaseActivity(R.layout.activity_premium) {

    companion object {
        const val ACTION_UPGRADE_SUCCESS = "com.example.expensetracker.UPGRADE_SUCCESS"
    }

    private lateinit var btnBack: ImageView
    private lateinit var btnCheckout: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutQrSection: LinearLayout
    private lateinit var ivQrCode: ImageView
    private lateinit var tvTransferContent: TextView
    private lateinit var tvAmount: TextView

    private val apiService = ApiClient.create(ApiService::class.java)
    private val scope = CoroutineScope(Dispatchers.Main)
    private lateinit var prefs: AppPreferences
    private val TAG = "PremiumActivity"

    /** BroadcastReceiver lắng nghe FCM upgrade thành công */
    private val upgradeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, ">>> Nhận broadcast UPGRADE_SUCCESS trong PremiumActivity <<<")
            showUpgradeSuccessDialog()
        }
    }

    override fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnCheckout = findViewById(R.id.btnCheckout)
        progressBar = findViewById(R.id.progressBar)
        layoutQrSection = findViewById(R.id.layoutQrSection)
        ivQrCode = findViewById(R.id.ivQrCode)
        tvTransferContent = findViewById(R.id.tvTransferContent)
        tvAmount = findViewById(R.id.tvAmount)

        prefs = AppPreferences(this)

        // Nếu đã PREMIUM thì thông báo
        if (prefs.isPremium) {
            btnCheckout.text = "Bạn đã là thành viên Premium"
            btnCheckout.isEnabled = false
        }
    }

    override fun initListeners() {
        btnBack.setOnClickListener { finish() }

        btnCheckout.setOnClickListener {
            checkout()
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(ACTION_UPGRADE_SUCCESS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(upgradeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(upgradeReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(upgradeReceiver)
        } catch (_: Exception) { }
    }

    private fun checkout() {
        progressBar.visibility = View.VISIBLE
        btnCheckout.isEnabled = false

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.checkout()
                }

                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val data = response.body()?.data
                    if (data != null) {
                        layoutQrSection.visibility = View.VISIBLE
                        btnCheckout.text = "Đang chờ thanh toán..."
                        btnCheckout.isEnabled = false
                        tvTransferContent.text = data.transferContent
                        val fmt = NumberFormat.getInstance(Locale("vi", "VN"))
                        tvAmount.text = "${fmt.format(data.amount)} VND"

                        Glide.with(this@PremiumActivity)
                            .load(data.qrUrl)
                            .into(ivQrCode)
                    } else {
                        Toast.makeText(this@PremiumActivity, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show()
                        btnCheckout.isEnabled = true
                    }
                } else {
                    Toast.makeText(this@PremiumActivity, "Lỗi tạo đơn hàng (${response.code()})", Toast.LENGTH_SHORT).show()
                    btnCheckout.isEnabled = true
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                btnCheckout.isEnabled = true
                Log.e(TAG, "Lỗi checkout", e)
                Toast.makeText(this@PremiumActivity, "Không thể kết nối đến máy chủ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showUpgradeSuccessDialog() {
        prefs.userType = "PREMIUM"

        AlertDialog.Builder(this)
            .setTitle("🎉 Nâng cấp thành công!")
            .setMessage("Tài khoản của bạn đã được nâng cấp lên Premium!\n\n✨ Mọi quảng cáo đã được tắt\n✨ Tính năng OCR đã được mở khóa")
            .setCancelable(false)
            .setPositiveButton("Tuyệt vời!") { _, _ ->
                setResult(RESULT_OK)
                finish()
            }
            .show()
    }
}
