package com.example.expensetracker.features.category

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.data.local.AppPreferences
import com.example.expensetracker.data.models.CategoryItem
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * CategoryActivity — Màn hình Quản lý danh mục.
 * Gọi API GET /api/v1/categories theo tab: Chi tiêu (EXPENSE) / Thu nhập (INCOME).
 */
class CategoryActivity : BaseActivity(R.layout.activity_category),
    CategoryController.CategoryListener {

    private lateinit var btnBack: ImageView
    private lateinit var tabExpense: TextView
    private lateinit var tabIncome: TextView
    private lateinit var recyclerCategories: RecyclerView
    private lateinit var fabAddCategory: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView

    private lateinit var adapter: CategoryAdapter
    private lateinit var controller: CategoryController
    private lateinit var prefs: AppPreferences
    private var isExpenseTab = true

    override fun initViews() {
        btnBack = findViewById(R.id.imgBack)
        tabExpense = findViewById(R.id.tabExpense)
        tabIncome = findViewById(R.id.tabIncome)
        recyclerCategories = findViewById(R.id.recyclerCategories)
        fabAddCategory = findViewById(R.id.fabAddCategory)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        prefs = AppPreferences(this)
        controller = CategoryController(this)

        adapter = CategoryAdapter(mutableListOf()) { item, _ ->
            Toast.makeText(this, "Chỉnh sửa: ${item.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerCategories.layoutManager = LinearLayoutManager(this)
        recyclerCategories.adapter = adapter

        // Tải danh mục Chi tiêu mặc định
        loadCategories("EXPENSE")
    }

    override fun initListeners() {
        btnBack.setOnClickListener { finish() }

        tabExpense.setOnClickListener {
            if (!isExpenseTab) {
                isExpenseTab = true
                updateTabStyle()
                loadCategories("EXPENSE")
            }
        }

        tabIncome.setOnClickListener {
            if (isExpenseTab) {
                isExpenseTab = false
                updateTabStyle()
                loadCategories("INCOME")
            }
        }

        fabAddCategory.setOnClickListener {
            val tabName = if (isExpenseTab) "chi tiêu" else "thu nhập"
            Toast.makeText(this, "Thêm danh mục $tabName mới", Toast.LENGTH_SHORT).show()
        }
    }

    // ─── CategoryListener callbacks ───────────────────────────────────────────

    override fun onCategoriesLoaded(items: List<CategoryItem>) {
        tvEmptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        recyclerCategories.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        adapter.updateList(items)
    }

    override fun onLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerCategories.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        tvEmptyState.visibility = View.VISIBLE
        recyclerCategories.visibility = View.GONE
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun loadCategories(type: String) {
        val token = prefs.authToken
        if (token.isBlank()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        controller.loadCategories(token, type)
    }

    private fun updateTabStyle() {
        if (isExpenseTab) {
            tabExpense.setBackgroundResource(R.drawable.bg_tab_selected)
            tabExpense.setTextColor(getColor(R.color.green_mid))
            tabExpense.typeface = android.graphics.Typeface.DEFAULT_BOLD
            tabIncome.setBackgroundResource(android.R.color.transparent)
            tabIncome.setTextColor(0xCCFFFFFF.toInt())
            tabIncome.typeface = android.graphics.Typeface.DEFAULT
        } else {
            tabIncome.setBackgroundResource(R.drawable.bg_tab_selected)
            tabIncome.setTextColor(getColor(R.color.green_mid))
            tabIncome.typeface = android.graphics.Typeface.DEFAULT_BOLD
            tabExpense.setBackgroundResource(android.R.color.transparent)
            tabExpense.setTextColor(0xCCFFFFFF.toInt())
            tabExpense.typeface = android.graphics.Typeface.DEFAULT
        }
    }
}
