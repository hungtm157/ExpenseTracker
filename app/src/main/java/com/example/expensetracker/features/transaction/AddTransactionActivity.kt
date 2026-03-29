package com.example.expensetracker.features.transaction

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.*
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.local.AppPreferences
import com.example.expensetracker.data.models.CategoryItem
import com.example.expensetracker.data.models.ScanInvoiceData
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
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import com.example.expensetracker.features.transaction.TransactionModel
import com.google.gson.Gson
import com.example.expensetracker.utils.DateTimeUtils
import android.content.Intent
import android.view.View

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
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val moneyFormat = DecimalFormat("#,###").apply {
        decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
    }

    private var walletsList = mutableListOf<WalletModel>()
    private var selectedWallet: WalletModel? = null

    private var editingTransaction: TransactionModel? = null
    private var isFromOcr = false

    // Camera OCR
    private var cameraImageUri: Uri? = null
    private var cameraImageFile: File? = null

    // Loading Dialog
    private var loadingDialog: AlertDialog? = null

    /** Launcher chụp ảnh camera → gửi scan invoice */
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraImageFile != null) {
            controller.scanInvoice(cameraImageFile!!)
        } else {
            Toast.makeText(this, "Không chụp được ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    /** Launcher xin quyền Camera */
    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            openCamera()
        } else {
            Toast.makeText(this, "Cần quyền Camera để quét hóa đơn", Toast.LENGTH_SHORT).show()
        }
    }

    /** Launcher chọn ảnh từ thư viện → gửi scan invoice */
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val file = uriToTempFile(it)
            if (file != null) {
                controller.scanInvoice(file)
            } else {
                Toast.makeText(this, "Không thể xử lý ảnh từ thư viện", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val amountWatcher = object : TextWatcher {
        private var current = ""
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val input = s.toString()
            if (input != current) {
                etAmount.removeTextChangedListener(this)

                val cleanString = input.replace(",", "")
                if (cleanString.isNotEmpty()) {
                    try {
                        val parsed = cleanString.toDouble()
                        val formatted = moneyFormat.format(parsed)

                        current = formatted
                        etAmount.setText(formatted)
                        etAmount.setSelection(formatted.length)
                    } catch (e: Exception) {
                        current = ""
                        etAmount.setText("")
                    }
                } else {
                    current = ""
                    etAmount.setText("")
                }

                etAmount.addTextChangedListener(this)
            }
        }
        override fun afterTextChanged(s: Editable?) {}
    }

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

        // Formatting Amount
        etAmount.addTextChangedListener(amountWatcher)

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
            val amountStr = etAmount.text.toString().replace(",", "")
            val amount = amountStr.toDoubleOrNull() ?: 0.0
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

            // Chuẩn hóa ngày về 00:00:00.000 UTC
            val apiCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = calendar.timeInMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dateStr = apiDateFormat.format(apiCal.time)

            if (editingTransaction != null) {
                // UPDATE MODE
                controller.updateTransaction(
                    id = editingTransaction!!.id,
                    walletId = selectedWallet?.id ?: 0, 
                    categoryId = categoryId,
                    amount = amount,
                    date = dateStr,
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
                    date = dateStr,
                    note = note,
                    currency = selectedWallet?.currency,
                    source = if (isFromOcr) TransactionSource.OCR_SCAN else TransactionSource.MANUAL
                )
            }
        }

        btnOcr.setOnClickListener {
            handleOcrClick()
        }
    }

    // ─── OCR Flow ────────────────────────────────────────────────────────────

    private fun handleOcrClick() {
        // 1. Check Premium
        if (!prefs.isPremium) {
            showPremiumRequiredDialog()
            return
        }
        
        // 2. Hiện lựa chọn nguồn ảnh
        val options = arrayOf("Chụp ảnh mới", "Chọn từ thư viện")
        AlertDialog.Builder(this)
            .setTitle("Quét hóa đơn")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { // Camera
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED
                        ) {
                            openCamera()
                        } else {
                            showCameraPermissionDialog()
                        }
                    }
                    1 -> { // Gallery
                        galleryLauncher.launch("image/*")
                    }
                }
            }
            .show()
    }

    private fun showPremiumRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("Tính năng Premium")
            .setMessage("Quét hóa đơn OCR chỉ dành cho tài khoản Premium.\nVui lòng nâng cấp để sử dụng tính năng này.")
            .setPositiveButton("Nâng cấp") { _, _ ->
                startActivity(Intent(this, com.example.expensetracker.features.premium.PremiumActivity::class.java))
            }
            .setNegativeButton("Để sau", null)
            .show()
    }

    private fun showCameraPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Quyền Camera")
            .setMessage("Ứng dụng cần quyền truy cập Camera để chụp ảnh hóa đơn.")
            .setPositiveButton("Cho phép") { _, _ ->
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun openCamera() {
        try {
            val imageDir = File(externalCacheDir, "camera_images")
            if (!imageDir.exists()) imageDir.mkdirs()
            cameraImageFile = File(imageDir, "invoice_${System.currentTimeMillis()}.jpg")
            cameraImageUri = FileProvider.getUriForFile(
                this, "${packageName}.fileprovider", cameraImageFile!!
            )
            cameraImageUri?.let { cameraLauncher.launch(it) }
        } catch (e: Exception) {
            Toast.makeText(this, "Không thể mở camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /** Copy Uri từ Gallery sang File tạm để gửi lên API */
    private fun uriToTempFile(uri: Uri): File? {
        return try {
            val mimeType = contentResolver.getType(uri)
            val extension = android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
            
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempDir = File(externalCacheDir, "temp_images")
            if (!tempDir.exists()) tempDir.mkdirs()
            val tempFile = File(tempDir, "temp_ocr_${System.currentTimeMillis()}.$extension")
            val outputStream = java.io.FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
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

    override fun onScanInvoiceResult(data: ScanInvoiceData) {
        isFromOcr = true
        
        // Điền số tiền
        etAmount.setText(data.amount.toInt().toString())

        // Điền ghi chú
        etNote.setText(data.note ?: "")

        // Parse & điền ngày (server trả yyyy-MM-dd)
        try {
            val ocrDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val parsedDate = ocrDateFormat.parse(data.transactionDate)
            if (parsedDate != null) {
                calendar.time = parsedDate
                updateDateText()
            }
        } catch (e: Exception) {
            // Nếu parse lỗi, giữ ngày hiện tại
        }

        // Tìm & chọn category theo ID
        val idx = categoryAdapter.findPositionByCategoryId(data.categoryId)
        if (idx != -1) {
            categoryAdapter.setSelectedPosition(idx)
            rvCategories.scrollToPosition(idx)
        }

        Toast.makeText(this, "Đã quét hóa đơn thành công!", Toast.LENGTH_SHORT).show()
    }

    override fun onLoading(isLoading: Boolean) {
        btnSave.isEnabled = !isLoading
        if (isLoading) {
            showLoadingDialog()
        } else {
            hideLoadingDialog()
        }
    }

    private fun showLoadingDialog() {
        if (loadingDialog == null) {
            val progressBar = ProgressBar(this).apply {
                isIndeterminate = true
                setPadding(0, 50, 0, 50)
            }
            loadingDialog = AlertDialog.Builder(this)
                .setTitle("Đang xử lý")
                .setMessage("Vui lòng đợi trong giây lát...")
                .setView(progressBar)
                .setCancelable(false)
                .create()
        }
        if (loadingDialog?.isShowing == false) {
            loadingDialog?.show()
        }
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
    }

    override fun onError(message: String) {
        Toast.makeText(this, "Lỗi: $message", Toast.LENGTH_SHORT).show()
    }
}
