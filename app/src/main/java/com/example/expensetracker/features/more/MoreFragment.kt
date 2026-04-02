package com.example.expensetracker.features.more

import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseFragment
import com.example.expensetracker.data.local.AppPreferences
import com.example.expensetracker.features.auth.login.LoginActivity
import com.example.expensetracker.features.category.CategoryActivity
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.models.UserProfileResponse
import com.example.expensetracker.data.repository.AuthRepository
import com.example.expensetracker.data.repository.BudgetRepository
import com.example.expensetracker.data.repository.TransactionRepository
import com.example.expensetracker.features.profile.ProfileController
import com.example.expensetracker.features.profile.ProfileListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale
import com.example.expensetracker.utils.AdManager

/**
 * MoreFragment — Màn hình Cài đặt (tab Khác trong Bottom Navigation).
 * Hiển thị: thông tin user, ngân sách tháng, tổng quan tài chính, menu cài đặt.
 */
class MoreFragment : BaseFragment(R.layout.fragment_more), ProfileListener {

    private lateinit var menuProfile: LinearLayout
    private lateinit var menuCategories: LinearLayout
    private lateinit var menuWallet: LinearLayout
    private lateinit var menuNotifications: LinearLayout
    private lateinit var menuSecurity: LinearLayout
    private lateinit var menuAbout: LinearLayout
    private lateinit var btnLogout: LinearLayout

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvAvatarInitials: TextView
    private lateinit var ivAvatar: ImageView

    private lateinit var tvBalance: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTransactionCount: TextView

    private lateinit var controller: ProfileController

    override fun initViews(view: View) {
        menuProfile = view.findViewById(R.id.menuProfile)
        menuCategories = view.findViewById(R.id.menuCategories)
        menuWallet = view.findViewById(R.id.menuWallet)
        menuNotifications = view.findViewById(R.id.menuNotifications)
        menuSecurity = view.findViewById(R.id.menuSecurity)
        menuAbout = view.findViewById(R.id.menuAbout)
        btnLogout = view.findViewById(R.id.btnLogout)
        
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        tvAvatarInitials = view.findViewById(R.id.tvAvatarInitials)
        ivAvatar = view.findViewById(R.id.ivAvatar)

        tvBalance = view.findViewById(R.id.tvBalance)
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense)
        tvTotalIncome = view.findViewById(R.id.tvTotalIncome)
        tvTransactionCount = view.findViewById(R.id.tvTransactionCount)

        // Setup controller
        val apiService = ApiClient.create(ApiService::class.java)
        val repository = AuthRepository(apiService)
        val transactionRepo = TransactionRepository(apiService)
        val budgetRepo = BudgetRepository(apiService)
        controller = ProfileController(repository, transactionRepo, budgetRepo, this)

        tvUserName.text = "Đang tải..."
        tvUserEmail.text = "..."

        controller.fetchProfile()
        loadStatistics()
    }

    private fun loadStatistics() {
        val apiService = ApiClient.create(ApiService::class.java)
        val statsRepo = com.example.expensetracker.data.repository.StatisticsRepository(apiService)
        val transRepo = com.example.expensetracker.data.repository.TransactionRepository(apiService)
        
        val vnLocale = Locale("vi", "VN")
        val numberFormat = NumberFormat.getInstance(vnLocale)

        lifecycleScope.launch {
            try {
                // Kiểm tra fragment vẫn còn attached
                val currentContext = context ?: return@launch
                
                // Fetch statistics
                val statsResult = withContext(Dispatchers.IO) { statsRepo.getStatisticsGeneral() }
                if (statsResult.isSuccessful) {
                    val data = statsResult.body()?.data
                    if (data != null && isAdded) {
                        tvBalance.text = numberFormat.format(data.totalBalance)
                        tvTotalIncome.text = numberFormat.format(data.totalIncome)
                        tvTotalExpense.text = numberFormat.format(data.totalExpense)
                    }
                }
                
                if (!isAdded) return@launch

                // Fetch transactions count
                val token = AppPreferences(currentContext).authToken ?: ""
                val transResult = withContext(Dispatchers.IO) { 
                    transRepo.getTransactions(token = token, page = 1, limit = 1) 
                }
                if (transResult.isSuccessful && isAdded) {
                    val total = transResult.body()?.data?.total ?: 0
                    tvTransactionCount.text = total.toString()
                }
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) return@launch
                e.printStackTrace()
            }
        }
    }

    override fun initListeners() {
        menuProfile.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    com.example.expensetracker.features.profile.ProfileActivity::class.java
                )
            )
        }
        menuCategories.setOnClickListener {
            AdManager.showInterstitialAd(requireActivity()) {
                startActivity(Intent(requireContext(), CategoryActivity::class.java))
            }
        }
        menuWallet.setOnClickListener {
            AdManager.showInterstitialAd(requireActivity()) {
                startActivity(
                    Intent(
                        requireContext(),
                        com.example.expensetracker.features.wallet.WalletActivity::class.java
                    )
                )
            }
        }
        menuNotifications.setOnClickListener {
            startActivity(Intent(requireContext(), com.example.expensetracker.features.notification.NotificationActivity::class.java))
        }
        menuSecurity.setOnClickListener {
            startActivity(Intent(requireContext(), PrivacyPolicyActivity::class.java))
        }
        menuAbout.setOnClickListener {
            startActivity(Intent(requireContext(), AboutAppActivity::class.java))
        }
        btnLogout.setOnClickListener {
            // Xoá toàn bộ: authToken, userId, userName, isLoggedIn
            AppPreferences(requireContext()).clear()

            // Về LoginActivity và xoá toàn bộ back stack
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }

    // --- ProfileListener Methods ---
    override fun onLoading(isLoading: Boolean) {}

    override fun onProfileLoaded(profile: UserProfileResponse) {
        if (!isAdded) return
        tvUserName.text = profile.fullName
        tvUserEmail.text = profile.email

        // Update initals
        val names = profile.fullName.trim().split(" ")
        if (names.isNotEmpty()) {
            val initials = if (names.size == 1) {
                names[0].take(1).uppercase()
            } else {
                "${names.first().take(1)}${names.last().take(1)}".uppercase()
            }
            tvAvatarInitials.text = initials
        }

        // Update Avatar picture
        val fullIconUrl = if (!profile.avatar.isNullOrEmpty()) {
            if (profile.avatar.startsWith("https")) profile.avatar else ApiClient.BASE_URL + profile.avatar
        } else null

        if (fullIconUrl != null) {
            Glide.with(this)
                .load(fullIconUrl)
                .circleCrop()
                .into(ivAvatar)
        }
    }

    override fun onError(message: String) {}
    override fun onNameUpdated(profile: UserProfileResponse) {}
    override fun onNameUpdateError(message: String) {}
    override fun onAvatarUpdated(profile: UserProfileResponse) {}
    override fun onAvatarUpdateError(message: String) {}
    override fun onStatsLoaded(transactionCount: Int, budgetCount: Int) {}
}
