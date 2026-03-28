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
import java.util.TimeZone
import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts

/**
 * PlanFragment — Màn hình Kế hoạch ngân sách (tab thứ ba trong Bottom Navigation).
 */
class PlanFragment : BaseFragment(R.layout.fragment_plan), BudgetListener, BudgetFilterDialog.OnFilterApplied {

    private lateinit var tvMonthYear: TextView
    private lateinit var btnCalendar: ImageView
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

    // Filter state
    private var filterFromDate: String? = null
    private var filterToDate: String? = null

    private val addBudgetLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            controller.loadBudgets(filterFromDate, filterToDate)
        }
    }

    override fun initViews(view: View) {
        tvMonthYear = view.findViewById(R.id.tvMonthYear)
        btnCalendar = view.findViewById(R.id.btnCalendar)
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

        // Init Adapter — click to open BudgetDetailActivity (using launcher for refresh)
        adapter = BudgetAdapter(emptyList()) { budget ->
            val intent = Intent(requireContext(), BudgetDetailActivity::class.java)
            intent.putExtra("budget_id", budget.id)
            addBudgetLauncher.launch(intent)
        }

        recyclerBudgets.layoutManager = LinearLayoutManager(requireContext())
        recyclerBudgets.adapter = adapter

        // Luôn bắt đầu với trạng thái bộ lọc sạch sẽ (Tất cả)
        filterFromDate = null
        filterToDate = null
        tvMonthYear.text = "(Tất cả)"

        // Load budgets
        controller.loadBudgets(filterFromDate, filterToDate)
    }

    override fun initListeners() {
        btnAddBudget.setOnClickListener {
            AdManager.showInterstitialAd(requireActivity()) {
                val intent = Intent(requireContext(), AddBudgetActivity::class.java)
                addBudgetLauncher.launch(intent)
            }
        }

        btnCreateFirst.setOnClickListener {
            AdManager.showInterstitialAd(requireActivity()) {
                val intent = Intent(requireContext(), AddBudgetActivity::class.java)
                addBudgetLauncher.launch(intent)
            }
        }

        btnCalendar.setOnClickListener {
            val dialog = BudgetFilterDialog()
            dialog.setOnFilterAppliedListener(this)
            dialog.show(childFragmentManager, "BudgetFilterDialog")
        }

        tabExpense.setOnClickListener {
            if (currentTab != "EXPENSE") {
                currentTab = "EXPENSE"
                updateTabUI()
                controller.loadBudgets(filterFromDate, filterToDate)
            }
        }

        tabIncome.setOnClickListener {
            if (currentTab != "INCOME") {
                currentTab = "INCOME"
                updateTabUI()
                controller.loadBudgets(filterFromDate, filterToDate)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        controller.loadBudgets(filterFromDate, filterToDate)
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

    // ─── BudgetFilterDialog.OnFilterApplied ───────────────────────────────────

    override fun onFilterApplied(fromDate: String?, toDate: String?, label: String) {
        this.filterFromDate = fromDate
        this.filterToDate = toDate
        tvMonthYear.text = "($label)"
        controller.loadBudgets(filterFromDate, filterToDate)
    }

    override fun onReset() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)

        // Ngày đầu tháng hiện tại
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        filterFromDate = sdf.format(cal.time)

        // Ngày cuối tháng hiện tại
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        filterToDate = sdf.format(cal.time)

        tvMonthYear.text = "($month/$year)"
        controller.loadBudgets(filterFromDate, filterToDate)
    }

    override fun onClear() {
        filterFromDate = null
        filterToDate = null
        tvMonthYear.text = "(Tất cả)"
        controller.loadBudgets(filterFromDate, filterToDate)
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

    override fun onBudgetDetailLoaded(budget: BudgetModel) {
        // Not used in this fragment
    }

    override fun onBudgetCreated(budget: BudgetModel) {
        Toast.makeText(requireContext(), "Đã tạo kế hoạch ngân sách", Toast.LENGTH_SHORT).show()
        controller.loadBudgets(filterFromDate, filterToDate)
    }

    override fun onBudgetUpdated(budget: BudgetModel) {
        Toast.makeText(requireContext(), "Đã cập nhật kế hoạch ngân sách", Toast.LENGTH_SHORT).show()
        controller.loadBudgets(filterFromDate, filterToDate)
    }

    override fun onBudgetCompleted(budget: BudgetModel) {
        Toast.makeText(requireContext(), "Đã chốt kế hoạch ngân sách", Toast.LENGTH_SHORT).show()
        controller.loadBudgets(filterFromDate, filterToDate)
    }

    override fun onLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
