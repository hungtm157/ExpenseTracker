package com.example.expensetracker.features.plan

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.repository.BudgetRepository
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * BudgetDetailActivity — Màn hình chi tiết kế hoạch ngân sách.
 */
class BudgetDetailActivity : BaseActivity(R.layout.activity_budget_detail), BudgetListener {

    private lateinit var btnBack: ImageView
    private lateinit var ivCategoryIcon: ImageView
    private lateinit var tvCategoryName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var progressBudget: ProgressBar
    private lateinit var tvPercentage: TextView
    private lateinit var tvSpent: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var tvAmountLimit: TextView
    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var tvAlertInfo: TextView
    private lateinit var btnEdit: TextView
    private lateinit var btnComplete: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var controller: BudgetController
    private val decimalFormat = DecimalFormat("#,###")
    private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val parseDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    private var budgetId: Int = -1
    private var currentBudget: BudgetModel? = null

    override fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        ivCategoryIcon = findViewById(R.id.ivCategoryIcon)
        tvCategoryName = findViewById(R.id.tvCategoryName)
        tvStatus = findViewById(R.id.tvStatus)
        progressBudget = findViewById(R.id.progressBudget)
        tvPercentage = findViewById(R.id.tvPercentage)
        tvSpent = findViewById(R.id.tvSpent)
        tvRemaining = findViewById(R.id.tvRemaining)
        tvAmountLimit = findViewById(R.id.tvAmountLimit)
        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)
        tvAlertInfo = findViewById(R.id.tvAlertInfo)
        btnEdit = findViewById(R.id.btnEdit)
        btnComplete = findViewById(R.id.btnComplete)
        progressBar = findViewById(R.id.progressBar)

        // Init controller
        val apiService = ApiClient.create(ApiService::class.java)
        val repository = BudgetRepository(apiService)
        controller = BudgetController(repository, this)

        // Get budget ID from intent
        budgetId = intent.getIntExtra("budget_id", -1)
        if (budgetId > 0) {
            controller.loadBudgetDetail(budgetId)
        } else {
            Toast.makeText(this, "Không tìm thấy kế hoạch ngân sách", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun initListeners() {
        btnBack.setOnClickListener { finish() }

        btnEdit.setOnClickListener {
            val budget = currentBudget ?: return@setOnClickListener
            val intent = Intent(this, AddBudgetActivity::class.java)
            intent.putExtra("budget_id", budget.id)
            intent.putExtra("category_id", budget.categoryId)
            intent.putExtra("amount_limit", budget.amountLimit)
            intent.putExtra("start_date", budget.startDate)
            intent.putExtra("end_date", budget.endDate)
            intent.putExtra("is_alert_enabled", budget.isAlertEnabled)
            intent.putExtra("alert_threshold", budget.alertThreshold)
            startActivity(intent)
        }

        btnComplete.setOnClickListener {
            val budget = currentBudget ?: return@setOnClickListener
            controller.completeBudget(budget.id)
        }
    }

    override fun onResume() {
        super.onResume()
        if (budgetId > 0) {
            controller.loadBudgetDetail(budgetId)
        }
    }

    private fun displayBudget(budget: BudgetModel) {
        currentBudget = budget

        // Category
        tvCategoryName.text = budget.category?.name ?: "Danh mục #${budget.categoryId}"

        // Status
        when (budget.status) {
            BudgetStatus.ACTIVE -> {
                tvStatus.text = "Đang hoạt động"
                tvStatus.setTextColor(Color.parseColor("#00C896"))
                btnEdit.visibility = View.VISIBLE
                btnComplete.visibility = View.VISIBLE
            }
            BudgetStatus.COMPLETED -> {
                tvStatus.text = "Đã hoàn thành"
                tvStatus.setTextColor(Color.parseColor("#FF9800"))
                btnEdit.visibility = View.GONE
                btnComplete.visibility = View.GONE
            }
            BudgetStatus.CANCELLED -> {
                tvStatus.text = "Đã hủy"
                tvStatus.setTextColor(Color.parseColor("#E53935"))
                btnEdit.visibility = View.GONE
                btnComplete.visibility = View.GONE
            }
        }

        // Progress
        val spent = budget.currentSpent ?: 0.0
        val ratio = budget.percentageUsed ?: 0.0
        val percentDisplay = (ratio * 100).toInt()
        val progressValue = percentDisplay.coerceIn(0, 100)
        val remaining = budget.remainingBudget ?: (budget.amountLimit - spent)

        progressBudget.progress = progressValue
        tvPercentage.text = "$percentDisplay%"
        tvSpent.text = "đ${decimalFormat.format(spent)}"
        tvRemaining.text = "đ${decimalFormat.format(remaining)}"

        // Color based on threshold
        val threshold = budget.alertThreshold
        val colorInt = when {
            ratio >= 1.0 -> Color.parseColor("#E53935")
            ratio >= threshold -> Color.parseColor("#FF9800")
            else -> Color.parseColor("#00C896")
        }
        progressBudget.progressTintList = ColorStateList.valueOf(colorInt)
        tvPercentage.setTextColor(colorInt)

        // Details
        tvAmountLimit.text = "đ${decimalFormat.format(budget.amountLimit)}"
        tvStartDate.text = formatDate(budget.startDate)
        tvEndDate.text = formatDate(budget.endDate)

        // Alert info
        if (budget.isAlertEnabled) {
            val thresholdPercent = (budget.alertThreshold * 100).toInt()
            tvAlertInfo.text = "Bật ($thresholdPercent%)"
        } else {
            tvAlertInfo.text = "Tắt"
        }
    }

    private fun formatDate(dateStr: String): String {
        return try {
            val parsed = parseDateFormat.parse(dateStr.substringBefore("."))
            if (parsed != null) displayDateFormat.format(parsed) else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    // ─── BudgetListener Callbacks ────────────────────────────────────────────

    override fun onBudgetDetailLoaded(budget: BudgetModel) {
        displayBudget(budget)
    }

    override fun onBudgetsLoaded(budgets: List<BudgetModel>) {}
    override fun onBudgetCreated(budget: BudgetModel) {}

    override fun onBudgetUpdated(budget: BudgetModel) {
        Toast.makeText(this, "Đã cập nhật kế hoạch ngân sách", Toast.LENGTH_SHORT).show()
        controller.loadBudgetDetail(budgetId)
    }

    override fun onBudgetCompleted(budget: BudgetModel) {
        Toast.makeText(this, "Đã chốt kế hoạch ngân sách", Toast.LENGTH_SHORT).show()
        controller.loadBudgetDetail(budgetId)
    }

    override fun onLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
