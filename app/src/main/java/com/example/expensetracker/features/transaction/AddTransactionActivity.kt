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
import com.example.expensetracker.data.local.AppPreferences
import com.example.expensetracker.data.models.CategoryItem
import com.example.expensetracker.data.repository.TransactionRepository
import com.example.expensetracker.data.repository.WalletRepository
import com.example.expensetracker.features.category.CategoryController
import com.example.expensetracker.features.wallet.WalletModel
import com.example.expensetracker.features.wallet.WalletType
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import com.example.expensetracker.features.transaction.TransactionModel
import com.google.gson.Gson
import com.example.expensetracker.utils.DateTimeUtils
import android.content.Intent

/**
 * AddTransactionActivity — Màn hình thêm giao dịch mới.
 */
class AddTransactionActivity : BaseActivity(R.layout.activity_add_transaction), 
    TransactionListener, CategoryController.CategoryListener {

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

    // Wallet Views
    private lateinit var btnWalletSelector: View
    private lateinit var ivSelectedWalletIcon: ImageView
    private lateinit var tvSelectedWalletName: TextView
    private lateinit var tvSelectedWalletBalance: TextView

    private lateinit var controller: TransactionController
    private lateinit var categoryController: CategoryController
    private lateinit var categoryAdapter: CategoryGridAdapter
    private lateinit var walletRepository: WalletRepository
    private lateinit var prefs: AppPreferences
    
    private var isExpense = true
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("vi", "VN"))
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val moneyFormat = DecimalFormat("#,###")

    private var walletsList = mutableListOf<WalletModel>()
    private var selectedWallet: WalletModel? = null

    private var editingTransaction: TransactionModel? = null

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

        // Wallet Views
        btnWalletSelector = findViewById(R.id.btnWalletSelector)
        ivSelectedWalletIcon = findViewById(R.id.ivSelectedWalletIcon)
        tvSelectedWalletName = findViewById(R.id.tvSelectedWalletName)
        tvSelectedWalletBalance = findViewById(R.id.tvSelectedWalletBalance)

        prefs = AppPreferences(this)
        
        val apiService = ApiClient.create(ApiService::class.java)
        
        // Init Transaction Controller
        val repository = TransactionRepository(apiService)
        controller = TransactionController(repository, this)

        // Init Wallet Repository
        walletRepository = WalletRepository(apiService)

        // Init Category Controller
        categoryController = CategoryController(this)

        // Init Date
        updateDateText()

        // Init Categories Adapter (empty initially)
        categoryAdapter = CategoryGridAdapter(mutableListOf()) {
            // Category selected
        }
        rvCategories.layoutManager = GridLayoutManager(this, 4)
        rvCategories.adapter = categoryAdapter

        // Load Initial Categories & Wallets
        loadCategories("EXPENSE")
        loadWallets()

        // Check for edit mode
        val transactionJson = intent.getStringExtra("TRANSACTION_DATA")
        if (!transactionJson.isNullOrEmpty()) {
            editingTransaction = Gson().fromJson(transactionJson, TransactionModel::class.java)
            setupEditMode()
        }
    }

    private fun setupEditMode() {
        val tx = editingTransaction ?: return
        
        // Cập nhật Mode (Thu/Chi)
        isExpense = !tx.category.isIncome
        updateToggleUI()
        loadCategories(if (isExpense) "EXPENSE" else "INCOME")

        // Gán dữ liệu cơ bản
        etAmount.setText(tx.amount.toInt().toString())
        etNote.setText(tx.note ?: "")

        // Cập nhật Date
        val parsedDate = DateTimeUtils.parseIsoDate(tx.transactionDate)
        if (parsedDate != null) {
            calendar.time = parsedDate
            updateDateText()
        }

        // Đổi TEXT btn
        btnSave.text = "Lưu thay đổi"
    }

    override fun initListeners() {
        btnClose.setOnClickListener { finish() }

        btnExpense.setOnClickListener { 
            if (!isExpense) {
                isExpense = true
                updateToggleUI()
                loadCategories("EXPENSE")
            }
        }
        btnIncome.setOnClickListener { 
            if (isExpense) {
                isExpense = false
                updateToggleUI()
                loadCategories("INCOME")
            }
        }

        btnDatePicker.setOnClickListener { showDatePicker() }

        btnWalletSelector.setOnClickListener {
            if (walletsList.isNotEmpty()) {
                showWalletSelectionBottomSheet()
            } else {
                Toast.makeText(this, "Đang tải danh sách ví...", Toast.LENGTH_SHORT).show()
            }
        }

        btnSave.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val note = etNote.text.toString().trim()
            val category = categoryAdapter.getSelectedCategory()

            if (selectedWallet == null) {
                Toast.makeText(this, "Vui lòng chọn ví", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (amount <= 0) {
                Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Nếu đang trong quá trình Edit Categories mà Adapter chưa render xong thì Category = null
            // Ta có thể giữ giá trị category id từ editingTransaction hoặc bắt buộc User đợi load
            val categoryId = category?.id ?: editingTransaction?.categoryId ?: 0
            if (categoryId == 0) {
                Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (editingTransaction != null) {
                // UPDATE MODE
                controller.updateTransaction(
                    id = editingTransaction!!.id,
                    walletId = selectedWallet?.id ?: 0, 
                    categoryId = categoryId,
                    amount = amount,
                    date = apiDateFormat.format(calendar.time),
                    note = note,
                    currency = selectedWallet?.currency
                )
            } else {
                // CREATE MODE
                controller.createTransaction(
                    token = prefs.authToken,
                    walletId = selectedWallet?.id ?: 0, 
                    categoryId = categoryId,
                    amount = amount,
                    date = apiDateFormat.format(calendar.time),
                    note = note,
                    currency = selectedWallet?.currency
                )
            }
        }

        btnOcr.setOnClickListener {
            Toast.makeText(this, "Tính năng OCR đang phát triển", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadWallets() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    walletRepository.getWallets()
                }
                if (response.isSuccessful) {
                    val wallets = response.body()?.data?.items ?: emptyList()
                    walletsList.clear()
                    walletsList.addAll(wallets)
                    if (walletsList.isNotEmpty()) {
                        updateSelectedWallet(walletsList[0])
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddTransactionActivity, "Không thể tải danh sách ví", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSelectedWallet(wallet: WalletModel) {
        selectedWallet = wallet
        tvSelectedWalletName.text = wallet.name
        tvSelectedWalletBalance.text = "${moneyFormat.format(wallet.balance)} ${wallet.currency}"
        
        val iconRes = when (wallet.type) {
            WalletType.CASH -> R.drawable.ic_boxed_cash
            WalletType.BANK_ACCOUNT -> R.drawable.ic_boxed_bank
            WalletType.E_WALLET -> R.drawable.ic_boxed_ewallet
        }
        ivSelectedWalletIcon.setImageResource(iconRes)
    }

    private fun showWalletSelectionBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_wallets, null)
        bottomSheetDialog.setContentView(view)

        // Nút đóng (X)
        view.findViewById<View>(R.id.btnBottomSheetClose).setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        val rvWallets: RecyclerView = view.findViewById(R.id.rvWallets)
        val adapter = WalletSelectionAdapter(walletsList, selectedWallet?.id ?: -1) { wallet ->
            updateSelectedWallet(wallet)
            bottomSheetDialog.dismiss()
        }
        rvWallets.adapter = adapter

        bottomSheetDialog.show()
    }

    private fun loadCategories(type: String) {
        val token = prefs.authToken
        if (token.isNotEmpty()) {
            categoryController.loadCategories(token, type)
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy Token", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateToggleUI() {
        if (isExpense) {
            btnExpense.setBackgroundResource(R.drawable.bg_tab_selected)
            btnExpense.setTextColor(resources.getColor(R.color.red, null))
            btnIncome.background = null
            btnIncome.setTextColor(resources.getColor(R.color.white, null))
        } else {
            btnIncome.setBackgroundResource(R.drawable.bg_tab_selected)
            btnIncome.setTextColor(resources.getColor(R.color.red, null))
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

    // ─── CategoryListener (CategoryController.CategoryListener) ──────────────

    override fun onCategoriesLoaded(items: List<CategoryItem>) {
        categoryAdapter.updateList(items)
        if (editingTransaction != null) {
            val selectedIndex = items.indexOfFirst { it.id == editingTransaction?.categoryId }
            if (selectedIndex != -1) {
                categoryAdapter.setSelectedPosition(selectedIndex)
                rvCategories.scrollToPosition(selectedIndex)
            }
        }
    }

    override fun onCategoryAdded(item: CategoryItem) {}
    override fun onCategoryUpdated(item: CategoryItem) {}
    override fun onCategoryDeleted(message: String) {}

    // ─── TransactionListener ────────────────────────────────────────────────

    override fun onTransactionCreated(transaction: TransactionModel) {
        Toast.makeText(this, "Đã lưu giao dịch!", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK, Intent().apply { putExtra("ACTION_RELOAD", true) })
        finish()
    }

    override fun onTransactionUpdated(transaction: TransactionModel) {
        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK, Intent().apply { putExtra("ACTION_RELOAD", true) })
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
