package com.example.expensetracker.features.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.expensetracker.core.network.ApiClient
import com.google.android.material.tabs.TabLayout
import androidx.lifecycle.lifecycleScope
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseFragment
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.repository.StatisticsRepository
import com.example.expensetracker.features.transaction.TransactionHistoryActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * HomeFragment — Màn hình Tổng quan (tab đầu tiên trong Bottom Navigation).
 */
class HomeFragment : BaseFragment(R.layout.fragment_home) {

    private lateinit var tvStatusExpense: TextView
    private lateinit var tvExpenseAndBalance: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var tvExpenseTotalNow: TextView
    private lateinit var pbCardStatus: ProgressBar
    private lateinit var tvProgressPercent: TextView

    private lateinit var tvTodayLabel: TextView
    private lateinit var tvCurrentMonth: TextView

    private lateinit var tvIncomeTodayLabel: TextView
    private lateinit var tvIncomeToday: TextView
    private lateinit var tvIncomeMonth: TextView
    private lateinit var tvIncomeCount: TextView
    private lateinit var tvIncomeAverage: TextView
    private lateinit var llIncomeCategoryContainer: LinearLayout

    private lateinit var tvTotalAsset: TextView
    private lateinit var tvTotalWalletsCount: TextView
    private lateinit var llAssetListContainer: LinearLayout
    private lateinit var llAssetAllocContainer: LinearLayout

    private lateinit var tabLayout: TabLayout
    private lateinit var layoutExpense: LinearLayout
    private lateinit var layoutIncome: LinearLayout
    private lateinit var layoutAsset: LinearLayout

    private lateinit var layoutCategory1: LinearLayout
    private lateinit var tvCatName1: TextView
    private lateinit var tvCatAmount1: TextView

    private lateinit var layoutCategory2: LinearLayout
    private lateinit var tvCatName2: TextView
    private lateinit var tvCatAmount2: TextView

    private lateinit var layoutCategory3: LinearLayout
    private lateinit var tvCatName3: TextView
    private lateinit var tvCatAmount3: TextView

    override fun initViews(view: View) {
        // TODO: bind dữ liệu thật từ Controller/API
        val btnViewAllTransactions = view.findViewById<TextView>(R.id.btnViewAllTransactions)
        btnViewAllTransactions.setOnClickListener {
            startActivity(
                android.content.Intent(
                    requireContext(),
                    TransactionHistoryActivity::class.java
                )
            )
        }

        val btnAddTransactionCenter = view.findViewById<Button>(R.id.btnAddTransactionCenter)
        btnAddTransactionCenter.setOnClickListener {
            startActivity(
                android.content.Intent(
                    requireContext(),
                    com.example.expensetracker.features.transaction.AddTransactionActivity::class.java
                )
            )
        }

        tvStatusExpense = view.findViewById(R.id.tvStatusExpense)
        tvExpenseAndBalance = view.findViewById(R.id.tvExpenseAndBalance)
        tvRemaining = view.findViewById(R.id.tvRemaining)
        tvExpenseTotalNow = view.findViewById(R.id.tvExpenseTotalNow)
        pbCardStatus = view.findViewById(R.id.pbCardStatus)
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent)

        tvTodayLabel = view.findViewById(R.id.tvTodayLabel)
        tvCurrentMonth = view.findViewById(R.id.tvCurrentMonth)

        tvIncomeTodayLabel = view.findViewById(R.id.tvIncomeTodayLabel)
        tvIncomeToday = view.findViewById(R.id.tvIncomeToday)
        tvIncomeMonth = view.findViewById(R.id.tvIncomeMonth)
        tvIncomeCount = view.findViewById(R.id.tvIncomeCount)
        tvIncomeAverage = view.findViewById(R.id.tvIncomeAverage)
        llIncomeCategoryContainer = view.findViewById(R.id.llIncomeCategoryContainer)
        
        tvTotalAsset = view.findViewById(R.id.tvTotalAsset)
        tvTotalWalletsCount = view.findViewById(R.id.tvTotalWalletsCount)
        llAssetListContainer = view.findViewById(R.id.llAssetListContainer)
        llAssetAllocContainer = view.findViewById(R.id.llAssetAllocContainer)

        tabLayout = view.findViewById(R.id.tabLayout)
        layoutExpense = view.findViewById(R.id.layoutExpense)
        layoutIncome = view.findViewById(R.id.layoutIncome)
        layoutAsset = view.findViewById(R.id.layoutAsset)

        layoutCategory1 = view.findViewById(R.id.layoutCategory1)
        tvCatName1 = view.findViewById(R.id.tvCatName1)
        tvCatAmount1 = view.findViewById(R.id.tvCatAmount1)

        layoutCategory2 = view.findViewById(R.id.layoutCategory2)
        tvCatName2 = view.findViewById(R.id.tvCatName2)
        tvCatAmount2 = view.findViewById(R.id.tvCatAmount2)

        layoutCategory3 = view.findViewById(R.id.layoutCategory3)
        tvCatName3 = view.findViewById(R.id.tvCatName3)
        tvCatAmount3 = view.findViewById(R.id.tvCatAmount3)

        setupTabs()
        loadStatistics()
    }

    private fun setupTabs() {
        val tabTitles = arrayOf("Chi tiêu", "Thu nhập", "Tài sản")
        val tabIcons = arrayOf(R.drawable.icon_chi, R.drawable.icon_thu, R.drawable.ic_wallet)
        val selectedColors = arrayOf("#FF5252", "#00C896", "#2196F3") // Red, Green, Blue
        val unselectedColor = Color.parseColor("#FFFFFF") // White

        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            if (tab != null) {
                val customView = layoutInflater.inflate(R.layout.layout_tab_item, null)
                val ivIcon = customView.findViewById<ImageView>(R.id.ivTabIcon)
                val tvTitle = customView.findViewById<TextView>(R.id.tvTabTitle)

                ivIcon.setImageResource(tabIcons[i])
                tvTitle.text = tabTitles[i]

                // Initial state
                if (i == tabLayout.selectedTabPosition) {
                    val color = Color.parseColor(selectedColors[i])
                    ivIcon.setColorFilter(color)
                    tvTitle.setTextColor(color)
                } else {
                    ivIcon.setColorFilter(unselectedColor)
                    tvTitle.setTextColor(unselectedColor)
                }

                tab.customView = customView
            }
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val customView = tab.customView
                if (customView != null) {
                    val ivIcon = customView.findViewById<ImageView>(R.id.ivTabIcon)
                    val tvTitle = customView.findViewById<TextView>(R.id.tvTabTitle)
                    val color = Color.parseColor(selectedColors[tab.position])
                    ivIcon.setColorFilter(color)
                    tvTitle.setTextColor(color)
                }

                layoutExpense.visibility = if (tab.position == 0) View.VISIBLE else View.GONE
                layoutIncome.visibility = if (tab.position == 1) View.VISIBLE else View.GONE
                layoutAsset.visibility = if (tab.position == 2) View.VISIBLE else View.GONE
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val customView = tab.customView
                if (customView != null) {
                    val ivIcon = customView.findViewById<ImageView>(R.id.ivTabIcon)
                    val tvTitle = customView.findViewById<TextView>(R.id.tvTabTitle)
                    ivIcon.setColorFilter(unselectedColor)
                    tvTitle.setTextColor(unselectedColor)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun loadStatistics() {
        // Date formats
        val now = Calendar.getInstance().time
        tvTodayLabel.text =
            "Hôm nay, " + SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN")).format(now)
        tvIncomeTodayLabel.text =
            "Thu nhập hôm nay (" + SimpleDateFormat("dd/MM", Locale("vi", "VN")).format(now) + ")"
        tvCurrentMonth.text =
            "Tháng " + SimpleDateFormat("MM, yyyy", Locale("vi", "VN")).format(now)

        val apiService = ApiClient.create(ApiService::class.java)
        val statsRepo = StatisticsRepository(apiService)
        val vnLocale = Locale("vi", "VN")
        val numberFormat = NumberFormat.getInstance(vnLocale)
        val walletRepo = com.example.expensetracker.data.repository.WalletRepository(apiService)

        // Tính toán ISO time string của start of day và end of day
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val fromDate = sdf.format(calendar.time)

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val toDate = sdf.format(calendar.time)

        // Tính toán FromDate và ToDate cho Start of month -> end of month (Current month)
        val monthCal = Calendar.getInstance()
        monthCal.set(Calendar.DAY_OF_MONTH, 1)
        monthCal.set(Calendar.HOUR_OF_DAY, 0)
        monthCal.set(Calendar.MINUTE, 0)
        monthCal.set(Calendar.SECOND, 0)
        val monthFromDate = sdf.format(monthCal.time)

        val lastDay = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        monthCal.set(Calendar.DAY_OF_MONTH, lastDay)
        monthCal.set(Calendar.HOUR_OF_DAY, 23)
        monthCal.set(Calendar.MINUTE, 59)
        monthCal.set(Calendar.SECOND, 59)
        val monthToDate = sdf.format(monthCal.time)

        lifecycleScope.launch {
            try {
                // Fetch Ratio
                val ratioResult =
                    withContext(Dispatchers.IO) { statsRepo.getStatisticsExpenseToBalanceRatio() }
                if (ratioResult.isSuccessful) {
                    val data = ratioResult.body()?.data
                    if (data != null) {
                        val totalBalance = data.totalBalance
                        val totalExpense = data.totalExpense
                        val remaining = totalBalance - totalExpense

                        tvExpenseAndBalance.text =
                            "Đã dùng: ${numberFormat.format(totalExpense)} / ${
                                numberFormat.format(totalBalance)
                            }"
                        tvRemaining.text = "Còn lại: ${numberFormat.format(remaining)}"

                        val ratio =
                            if (totalBalance > 0) totalExpense.toDouble() / totalBalance.toDouble() else 0.0
                        val percentDisplay = (ratio * 100).toInt()
                        val progressValue = percentDisplay.coerceIn(0, 100)
                        tvProgressPercent.text = "${percentDisplay}%"

                        val colorGreen = Color.parseColor("#00C896")
                        val colorOrange = Color.parseColor("#FF9800")
                        val colorRed = Color.parseColor("#E53935")

                        val bgGreen = Color.parseColor("#E8F5E9")
                        val bgOrange = Color.parseColor("#FFF3E0")
                        val bgRed = Color.parseColor("#FFEBEE")

                        val progressColor: Int
                        val bgColor: Int

                        when {
                            ratio >= 1.0 -> {
                                tvStatusExpense.text = "Bạn đang chi tiêu Vượt mức"
                                tvStatusExpense.setTextColor(colorRed)
                                progressColor = colorRed
                                bgColor = bgRed
                            }

                            ratio >= 0.8 -> {
                                tvStatusExpense.text = "Bạn đang chi tiêu Cảnh báo"
                                tvStatusExpense.setTextColor(colorOrange)
                                progressColor = colorOrange
                                bgColor = bgOrange
                            }

                            else -> {
                                tvStatusExpense.text = "Bạn đang chi tiêu An toàn"
                                tvStatusExpense.setTextColor(colorGreen)
                                progressColor = colorGreen
                                bgColor = bgGreen
                            }
                        }

                        pbCardStatus.progress = progressValue
                        pbCardStatus.progressTintList = ColorStateList.valueOf(progressColor)
                        pbCardStatus.progressBackgroundTintList = ColorStateList.valueOf(bgColor)
                    }
                }

                // Fetch Today Expense
                val todayResult = withContext(Dispatchers.IO) {
                    statsRepo.getStatisticsIncomeVsExpense(
                        fromDate,
                        toDate
                    )
                }
                if (todayResult.isSuccessful) {
                    val data = todayResult.body()?.data
                    if (data != null) {
                        val expenseAmount = data.expense?.totalAmount ?: 0L
                        tvExpenseTotalNow.text = numberFormat.format(expenseAmount)
                        
                        val incomeAmount = data.income?.totalAmount ?: 0L
                        tvIncomeToday.text = numberFormat.format(incomeAmount)
                    }
                }
                
                // Fetch General Statistics (Current Month) for Income
                var totalIncomeMonth = 0L
                val generalResult = withContext(Dispatchers.IO) {
                    statsRepo.getStatisticsGeneral(monthFromDate, monthToDate)
                }
                if (generalResult.isSuccessful) {
                    val data = generalResult.body()?.data
                    if (data != null) {
                        totalIncomeMonth = data.totalIncome ?: 0L
                        tvIncomeMonth.text = numberFormat.format(totalIncomeMonth)
                    }
                }
                
                // Fetch Income Categories (Current Month)
                val incomeCategoryResult = withContext(Dispatchers.IO) {
                    statsRepo.getStatisticsByCategory("INCOME", monthFromDate, monthToDate)
                }
                if (incomeCategoryResult.isSuccessful) {
                    val catList = incomeCategoryResult.body()?.data ?: emptyList()
                    val count = catList.size
                    tvIncomeCount.text = "Số lần thu nhập: $count"
                    
                    val average = if (count > 0) totalIncomeMonth / count else 0L
                    tvIncomeAverage.text = "Trung bình: ${numberFormat.format(average)}"
                    
                    if (llIncomeCategoryContainer.childCount > 1) {
                        llIncomeCategoryContainer.removeViews(1, llIncomeCategoryContainer.childCount - 1)
                    }
                    
                    catList.forEach { item ->
                        val itemView = layoutInflater.inflate(R.layout.item_home_income_category, llIncomeCategoryContainer, false)
                        val ivIcon = itemView.findViewById<ImageView>(R.id.ivCatIcon)
                        val tvName = itemView.findViewById<TextView>(R.id.tvCatName)
                        val tvAmount = itemView.findViewById<TextView>(R.id.tvCatAmount)
                        
                        tvName.text = item.categoryName
                        tvAmount.text = numberFormat.format(item.totalAmount)
                        
                        if (!item.categoryIcon.isNullOrEmpty()) {
                            Glide.with(this@HomeFragment)
                                .load(ApiClient.BASE_URL + item.categoryIcon.trimStart('/'))
                                .placeholder(R.drawable.ic_statistics)
                                .error(R.drawable.ic_statistics)
                                .into(ivIcon)
                        } else {
                            ivIcon.setImageResource(R.drawable.ic_statistics)
                        }
                        
                        llIncomeCategoryContainer.addView(itemView)
                    }
                }
                
                // Fetch General Statistics (All Time for Asset Tab)
                var totalAssetValue = 0L
                val allTimeGeneralResult = withContext(Dispatchers.IO) {
                    statsRepo.getStatisticsGeneral(null, null)
                }
                if (allTimeGeneralResult.isSuccessful) {
                    val data = allTimeGeneralResult.body()?.data
                    if (data != null) {
                        totalAssetValue = data.totalBalance ?: 0L
                        tvTotalAsset.text = numberFormat.format(totalAssetValue)
                    }
                }
                
                // Fetch Wallets
                val walletsResult = withContext(Dispatchers.IO) {
                    walletRepo.getWallets(1, 100, null, null, null)
                }
                if (walletsResult.isSuccessful) {
                    val wallets = walletsResult.body()?.data?.items ?: emptyList()
                    tvTotalWalletsCount.text = "Số tài khoản: ${wallets.size}"
                    
                    if (llAssetListContainer.childCount > 1) {
                        llAssetListContainer.removeViews(1, llAssetListContainer.childCount - 1)
                    }
                    if (llAssetAllocContainer.childCount > 1) {
                        llAssetAllocContainer.removeViews(1, llAssetAllocContainer.childCount - 1)
                    }
                    
                    wallets.forEach { wallet ->
                        val percentFloat = if (totalAssetValue > 0) {
                            (wallet.balance.toDouble() / totalAssetValue * 100).toFloat()
                        } else 0f
                        val percentString = String.format(java.util.Locale.US, "%.1f", percentFloat)
                        
                        // 1. Asset List Item
                        val listItemView = layoutInflater.inflate(R.layout.item_home_asset_list, llAssetListContainer, false)
                        val ivIcon = listItemView.findViewById<ImageView>(R.id.ivWalletIcon)
                        val tvName = listItemView.findViewById<TextView>(R.id.tvWalletName)
                        val tvPercent = listItemView.findViewById<TextView>(R.id.tvWalletPercent)
                        val tvBalance = listItemView.findViewById<TextView>(R.id.tvWalletBalance)
                        
                        tvName.text = wallet.name
                        tvPercent.text = "${percentString}% tổng tài sản"
                        tvBalance.text = numberFormat.format(wallet.balance)
                        
                        when (wallet.type?.name) {
                            "CASH" -> ivIcon.setImageResource(R.drawable.ic_wallet_type_cash)
                            "BANK_ACCOUNT" -> ivIcon.setImageResource(R.drawable.ic_wallet_type_bank)
                            "E_WALLET" -> ivIcon.setImageResource(R.drawable.ic_wallet_type_ewallet)
                            else -> ivIcon.setImageResource(R.drawable.ic_wallet)
                        }
                        
                        llAssetListContainer.addView(listItemView)
                        
                        // 2. Asset Allocation Item
                        val allocItemView = layoutInflater.inflate(R.layout.item_home_asset_alloc, llAssetAllocContainer, false)
                        val tvAllocName = allocItemView.findViewById<TextView>(R.id.tvAllocName)
                        val tvAllocPercentText = allocItemView.findViewById<TextView>(R.id.tvAllocPercentText)
                        val pbAlloc = allocItemView.findViewById<ProgressBar>(R.id.pbAlloc)
                        
                        tvAllocName.text = wallet.name
                        tvAllocPercentText.text = "${percentString}%"
                        pbAlloc.max = 1000
                        pbAlloc.progress = (percentFloat * 10).toInt()
                        
                        llAssetAllocContainer.addView(allocItemView)
                    }
                }

                // Fetch Top 3 Categories (Current Month)
                val categoryResult = withContext(Dispatchers.IO) {
                    statsRepo.getStatisticsByCategory(
                        "EXPENSE",
                        monthFromDate,
                        monthToDate
                    )
                }
                if (categoryResult.isSuccessful) {
                    val catList = categoryResult.body()?.data ?: emptyList()
                    val catUIDataList = listOf(
                        Triple(layoutCategory1, tvCatName1, tvCatAmount1),
                        Triple(layoutCategory2, tvCatName2, tvCatAmount2),
                        Triple(layoutCategory3, tvCatName3, tvCatAmount3)
                    )

                    // Hide all by default
                    catUIDataList.forEach { it.first.visibility = View.GONE }

                    // Show up to 3 items
                    for (i in 0 until minOf(catList.size, 3)) {
                        val item = catList[i]
                        val ui = catUIDataList[i]
                        ui.first.visibility = View.VISIBLE
                        ui.second.text = item.categoryName
                        ui.third.text = numberFormat.format(item.totalAmount)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun initListeners() {
        // TODO: xử lý sự kiện chip lọc, xem chi tiết danh mục
    }
}
