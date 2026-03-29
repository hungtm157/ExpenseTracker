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
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.local.AppPreferences
import com.example.expensetracker.data.models.UserProfileResponse
import com.example.expensetracker.data.repository.AuthRepository
import com.example.expensetracker.data.repository.BudgetRepository
import com.example.expensetracker.data.repository.TransactionRepository
import com.example.expensetracker.features.profile.ProfileController
import com.example.expensetracker.features.profile.ProfileListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

/**
 * PremiumActivity — Màn hình nâng cấp tài khoản Premium.
 */
class PremiumActivity : BaseActivity(R.layout.activity_premium), ProfileListener {

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
    
    private lateinit var profileController: ProfileController

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
        
        // Setup controller
        val repository = AuthRepository(apiService)
        val transactionRepo = TransactionRepository(apiService)
        val budgetRepo = BudgetRepository(apiService)
        profileController = ProfileController(repository, transactionRepo, budgetRepo, this)

        // Nếu đã PREMIUM thì tạm thời khóa button (sẽ check lại khi onProfileLoaded)
        updateCheckoutButtonState(prefs.isPremium)
    }

    private fun updateCheckoutButtonState(isPremium: Boolean) {
        if (isPremium) {
            btnCheckout.text = "Bạn đã là thành viên Premium"
            btnCheckout.isEnabled = false
        } else {
            btnCheckout.text = "Nâng cấp ngay"
            btnCheckout.isEnabled = true
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
        ContextCompat.registerReceiver(
            this,
            upgradeReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        
        // Refresh profile to get the latest status
        profileController.fetchProfile()
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
        // AppPreferences.userType is now updated in ProfileController or by FCM
        // but let's be safe
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

    // --- ProfileListener Methods ---
    override fun onLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onProfileLoaded(profile: UserProfileResponse) {
        // Update UI based on latest profile data
        updateCheckoutButtonState(profile.type == "PREMIUM")
    }

    override fun onError(message: String) {
        // Quiet failure, rely on cached state
        Log.e(TAG, "Lỗi cập nhật profile: $message")
    }

    override fun onNameUpdated(profile: UserProfileResponse) {}
    override fun onNameUpdateError(message: String) {}
    override fun onAvatarUpdated(profile: UserProfileResponse) {}
    override fun onAvatarUpdateError(message: String) {}
    override fun onStatsLoaded(transactionCount: Int, budgetCount: Int) {}
}
