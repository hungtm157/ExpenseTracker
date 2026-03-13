package com.example.expensetracker.features.transaction

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.repository.TransactionRepository
import com.example.expensetracker.features.category.CategoryModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * AddTransactionActivity — Màn hình thêm giao dịch mới.
 */
class AddTransactionActivity : BaseActivity(R.layout.activity_add_transaction), TransactionListener {

    private lateinit var btnClose: ImageView
    private lateinit var btnExpense: TextView
    private lateinit var btnIncome: TextView
    private lateinit var etAmount: EditText
    private lateinit var rvCategories: RecyclerView
    private lateinit var etNote: EditText
    private lateinit var btnDatePicker: View
    private lateinit var tvDate: TextView
    private lateinit var btnSave: Button
    private lateinit var btnOcr: Button

    private lateinit var controller: TransactionController
    private lateinit var categoryAdapter: CategoryGridAdapter
    
    private var isExpense = true
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("vi", "VN"))
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun initViews() {
        btnClose = findViewById(R.id.btnClose)
        btnExpense = findViewById(R.id.btnExpense)
        btnIncome = findViewById(R.id.btnIncome)
        etAmount = findViewById(R.id.etAmount)
        rvCategories = findViewById(R.id.rvCategories)
        etNote = findViewById(R.id.etNote)
        btnDatePicker = findViewById(R.id.btnDatePicker)
        tvDate = findViewById(R.id.tvDate)
        btnSave = findViewById(R.id.btnSave)
        btnOcr = findViewById(R.id.btnOcr)

        // Init Controller
        val apiService = ApiClient.create(ApiService::class.java)
        val repository = TransactionRepository(apiService)
        controller = TransactionController(repository, this)

        // Init Date
        updateDateText()

        // Init Categories (Mock for UI demo)
        val mockCategories = listOf(
            CategoryModel("🍴", "Ăn tiệm", "", 0),
            CategoryModel("🏠", "Sinh hoạt", "", 0),
            CategoryModel("🚗", "Đi lại", "", 0),
            CategoryModel("🛍️", "Mua sắm", "", 0),
            CategoryModel("🏥", "Sức khỏe", "", 0),
            CategoryModel("🎮", "Giải trí", "", 0),
            CategoryModel("🎓", "Giáo dục", "", 0),
            CategoryModel("💼", "Công việc", "", 0)
        )
        categoryAdapter = CategoryGridAdapter(mockCategories) {
            // Category selected
        }
        rvCategories.layoutManager = GridLayoutManager(this, 4)
        rvCategories.adapter = categoryAdapter
    }

    override fun initListeners() {
        btnClose.setOnClickListener { finish() }

        btnExpense.setOnClickListener { 
            isExpense = true
            updateToggleUI()
        }
        btnIncome.setOnClickListener { 
            isExpense = false
            updateToggleUI()
        }

        btnDatePicker.setOnClickListener { showDatePicker() }

        btnSave.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val note = etNote.text.toString().trim()
            val category = categoryAdapter.getSelectedCategory()

            if (amount <= 0) {
                Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (category == null) {
                Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // For now, using hardcoded wallet ID 1
            controller.createTransaction(
                walletId = 1, 
                categoryId = 1, // Using ID 1 for mock
                amount = if (isExpense) amount else amount, // Logic depends on backend handle
                date = apiDateFormat.format(calendar.time),
                note = note
            )
        }

        btnOcr.setOnClickListener {
            Toast.makeText(this, "Tính năng OCR đang phát triển", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateToggleUI() {
        if (isExpense) {
            btnExpense.setBackgroundResource(R.drawable.bg_tab_selected)
            btnExpense.setTextColor(resources.getColor(R.color.nav_active, null)) // Or red if design says so
            btnIncome.background = null
            btnIncome.setTextColor(resources.getColor(R.color.white, null))
        } else {
            btnIncome.setBackgroundResource(R.drawable.bg_tab_selected)
            btnIncome.setTextColor(resources.getColor(R.color.nav_active, null))
            btnExpense.background = null
            btnExpense.setTextColor(resources.getColor(R.color.white, null))
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                updateDateText()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateText() {
        tvDate.text = dateFormat.format(calendar.time)
    }

    // ─── TransactionListener ────────────────────────────────────────────────

    override fun onTransactionCreated(transaction: TransactionModel) {
        Toast.makeText(this, "Đã lưu giao dịch!", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onTransactionUpdated(transaction: TransactionModel) {
        finish()
    }

    override fun onTransactionDeleted() {
        finish()
    }

    override fun onOcrResult(transaction: TransactionModel) {
        etAmount.setText(transaction.amount.toString())
        etNote.setText(transaction.note)
    }

    override fun onLoading(isLoading: Boolean) {
        btnSave.isEnabled = !isLoading
    }

    override fun onError(message: String) {
        Toast.makeText(this, "Lỗi: $message", Toast.LENGTH_SHORT).show()
    }
}
