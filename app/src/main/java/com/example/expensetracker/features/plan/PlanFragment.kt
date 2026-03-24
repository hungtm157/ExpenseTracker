package com.example.expensetracker.features.plan

import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseFragment
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.repository.BudgetRepository
import com.example.expensetracker.utils.AdManager
import java.text.DecimalFormat
import java.util.Calendar

/**
 * PlanFragment — Màn hình Kế hoạch ngân sách (tab thứ ba trong Bottom Navigation).
 */
class PlanFragment : BaseFragment(R.layout.fragment_plan), BudgetListener {

    private lateinit var tvMonthYear: TextView
    private lateinit var btnAddBudget: ImageView
    private lateinit var tabExpense: TextView
    private lateinit var tabIncome: TextView
    private lateinit var tvTotalBudget: TextView
    private lateinit var tvAvgPerDay: TextView
    private lateinit var recyclerBudgets: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var btnCreateFirst: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var adapter: BudgetAdapter
    private lateinit var controller: BudgetController

    private val decimalFormat = DecimalFormat("#,###")
    private var currentTab = "EXPENSE" // EXPENSE or INCOME

    override fun initViews(view: View) {
        tvMonthYear = view.findViewById(R.id.tvMonthYear)
        btnAddBudget = view.findViewById(R.id.btnAddBudget)
        tabExpense = view.findViewById(R.id.tabExpense)
        tabIncome = view.findViewById(R.id.tabIncome)
        tvTotalBudget = view.findViewById(R.id.tvTotalBudget)
        tvAvgPerDay = view.findViewById(R.id.tvAvgPerDay)
        recyclerBudgets = view.findViewById(R.id.recyclerBudgets)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)
        btnCreateFirst = view.findViewById(R.id.btnCreateFirst)
        progressBar = view.findViewById(R.id.progressBar)

        // Set current month/year
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        tvMonthYear.text = "($month/$year)"

        // Init Repository & Controller
        val apiService = ApiClient.create(ApiService::class.java)
        val repository = BudgetRepository(apiService)
        controller = BudgetController(repository, this)

        // Init Adapter — click to edit
        adapter = BudgetAdapter(emptyList()) { budget ->
            val intent = Intent(requireContext(), AddBudgetActivity::class.java)
            intent.putExtra("budget_id", budget.id)
            intent.putExtra("category_id", budget.categoryId)
            intent.putExtra("amount_limit", budget.amountLimit)
            intent.putExtra("start_date", budget.startDate)
            intent.putExtra("end_date", budget.endDate)
            intent.putExtra("is_alert_enabled", budget.isAlertEnabled)
            intent.putExtra("alert_threshold", budget.alertThreshold)
            startActivity(intent)
        }

        recyclerBudgets.layoutManager = LinearLayoutManager(requireContext())
        recyclerBudgets.adapter = adapter

        // Load budgets
        controller.loadBudgets()
    }

    override fun initListeners() {
        btnAddBudget.setOnClickListener {
            AdManager.showInterstitialAd(requireActivity()) {
                startActivity(Intent(requireContext(), AddBudgetActivity::class.java))
            }
        }

        btnCreateFirst.setOnClickListener {
            AdManager.showInterstitialAd(requireActivity()) {
                startActivity(Intent(requireContext(), AddBudgetActivity::class.java))
            }
        }

        tabExpense.setOnClickListener {
            if (currentTab != "EXPENSE") {
                currentTab = "EXPENSE"
                updateTabUI()
                controller.loadBudgets()
            }
        }

        tabIncome.setOnClickListener {
            if (currentTab != "INCOME") {
                currentTab = "INCOME"
                updateTabUI()
                controller.loadBudgets()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        controller.loadBudgets()
    }

    private fun updateTabUI() {
        if (currentTab == "EXPENSE") {
            tabExpense.setBackgroundResource(R.drawable.bg_button_green)
            tabExpense.setTextColor(resources.getColor(R.color.white, null))
            tabIncome.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            tabIncome.setTextColor(resources.getColor(R.color.text_secondary, null))
        } else {
            tabIncome.setBackgroundResource(R.drawable.bg_button_green)
            tabIncome.setTextColor(resources.getColor(R.color.white, null))
            tabExpense.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            tabExpense.setTextColor(resources.getColor(R.color.text_secondary, null))
        }
    }

    // ─── BudgetListener Callbacks ────────────────────────────────────────────

    override fun onBudgetsLoaded(budgets: List<BudgetModel>) {
        // Filter by tab type based on category type
        val filtered = budgets.filter { budget ->
            val categoryType = budget.category?.type ?: "EXPENSE"
            categoryType == currentTab
        }

        adapter.updateData(filtered)

        if (filtered.isEmpty()) {
            recyclerBudgets.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
        } else {
            recyclerBudgets.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE
        }

        // Update summary
        val totalLimit = filtered.sumOf { it.amountLimit }
        tvTotalBudget.text = "đ${decimalFormat.format(totalLimit)}"

        // Calculate avg per day: total budget limit / days in month
        val cal = Calendar.getInstance()
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val avgPerDay = if (daysInMonth > 0) totalLimit / daysInMonth else 0.0
        tvAvgPerDay.text = "đ${decimalFormat.format(avgPerDay)}"
    }

    override fun onBudgetCreated(budget: BudgetModel) {
        Toast.makeText(requireContext(), "Đã tạo kế hoạch ngân sách", Toast.LENGTH_SHORT).show()
        controller.loadBudgets()
    }

    override fun onBudgetUpdated(budget: BudgetModel) {
        Toast.makeText(requireContext(), "Đã cập nhật kế hoạch ngân sách", Toast.LENGTH_SHORT).show()
        controller.loadBudgets()
    }

    override fun onBudgetCompleted(budget: BudgetModel) {
        Toast.makeText(requireContext(), "Đã chốt kế hoạch ngân sách", Toast.LENGTH_SHORT).show()
        controller.loadBudgets()
    }

    override fun onLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
