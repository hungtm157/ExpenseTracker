package com.example.expensetracker.features.category

import android.content.Intent
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * CategoryActivity — Màn hình Quản lý danh mục.
 * Hỗ trợ switch giữa Chi tiêu và Thu nhập.
 */
class CategoryActivity : BaseActivity(R.layout.activity_category) {

    private lateinit var btnBack: ImageView
    private lateinit var tabExpense: TextView
    private lateinit var tabIncome: TextView
    private lateinit var recyclerCategories: RecyclerView
    private lateinit var fabAddCategory: FloatingActionButton

    private lateinit var adapter: CategoryAdapter
    private var isExpenseTab = true

    // Dữ liệu mẫu Chi tiêu
    private val expenseCategories = mutableListOf(
        CategoryModel("🍽️", "Ăn uống", "Mặc định • Đang dùng", R.drawable.bg_cat_orange),
        CategoryModel("🛒", "Mua sắm", "Mặc định • Đang dùng", R.drawable.bg_cat_red),
        CategoryModel("🚗", "Đi lại", "Mặc định • Đang dùng", R.drawable.bg_cat_blue),
        CategoryModel("🏠", "Nhà ở", "Mặc định • Đang dùng", R.drawable.bg_cat_teal),
        CategoryModel("🏥", "Sức khoẻ", "Mặc định • Đang dùng", R.drawable.bg_cat_red),
        CategoryModel("🎓", "Giáo dục", "Mặc định • Đang dùng", R.drawable.bg_cat_blue)
    )

    // Dữ liệu mẫu Thu nhập
    private val incomeCategories = mutableListOf(
        CategoryModel("💰", "Lương", "Mặc định • Đang dùng", R.drawable.bg_cat_teal),
        CategoryModel("📈", "Đầu tư", "Mặc định • Đang dùng", R.drawable.bg_cat_blue),
        CategoryModel("🎁", "Thưởng", "Mặc định • Đang dùng", R.drawable.bg_cat_orange),
        CategoryModel("💼", "Kinh doanh", "Mặc định • Đang dùng", R.drawable.bg_cat_red)
    )

    override fun initViews() {
        btnBack = findViewById(R.id.imgBack)
        tabExpense = findViewById(R.id.tabExpense)
        tabIncome = findViewById(R.id.tabIncome)
        recyclerCategories = findViewById(R.id.recyclerCategories)
        fabAddCategory = findViewById(R.id.fabAddCategory)

        adapter = CategoryAdapter(expenseCategories) { item, _ ->
            Toast.makeText(this, "Chỉnh sửa: ${item.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerCategories.layoutManager = LinearLayoutManager(this)
        recyclerCategories.adapter = adapter
    }

    override fun initListeners() {
        btnBack.setOnClickListener { finish() }

        tabExpense.setOnClickListener {
            if (!isExpenseTab) switchTab(toExpense = true)
        }

        tabIncome.setOnClickListener {
            if (isExpenseTab) switchTab(toExpense = false)
        }

        fabAddCategory.setOnClickListener {
            val tabName = if (isExpenseTab) "chi tiêu" else "thu nhập"
            Toast.makeText(this, "Thêm danh mục $tabName mới", Toast.LENGTH_SHORT).show()
        }
    }

    private fun switchTab(toExpense: Boolean) {
        isExpenseTab = toExpense

        if (toExpense) {
            // Chi tiêu active
            tabExpense.setBackgroundResource(R.drawable.bg_tab_selected)
            tabExpense.setTextColor(getColor(R.color.green_mid))
            tabExpense.typeface = android.graphics.Typeface.DEFAULT_BOLD
            tabIncome.setBackgroundResource(android.R.color.transparent)
            tabIncome.setTextColor(0xCCFFFFFF.toInt())
            tabIncome.typeface = android.graphics.Typeface.DEFAULT
            adapter.updateList(expenseCategories)
        } else {
            // Thu nhập active
            tabIncome.setBackgroundResource(R.drawable.bg_tab_selected)
            tabIncome.setTextColor(getColor(R.color.green_mid))
            tabIncome.typeface = android.graphics.Typeface.DEFAULT_BOLD
            tabExpense.setBackgroundResource(android.R.color.transparent)
            tabExpense.setTextColor(0xCCFFFFFF.toInt())
            tabExpense.typeface = android.graphics.Typeface.DEFAULT
            adapter.updateList(incomeCategories)
        }
    }
}
