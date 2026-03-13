package com.example.expensetracker.features.category

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.data.local.AppPreferences
import com.example.expensetracker.data.models.CategoryItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * CategoryActivity — Màn hình Quản lý danh mục.
 * Hỗ trợ các chức năng: Xem danh sách, Thêm mới, Chỉnh sửa, và Bật/Tắt trạng thái.
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

    // BASE_URL đồng bộ với ApiClient/Adapter
    private val BASE_URL = "https://maddie-conditioned-increasingly.ngrok-free.dev"

    // Biến tạm cho Dialog
    private var selectedImageUri: Uri? = null
    private var dialogImgSelected: ImageView? = null
    private var dialogImgRemove: ImageView? = null
    private var dialogLayoutPlaceholder: LinearLayout? = null

    // Picker chọn ảnh
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            dialogImgSelected?.visibility = View.VISIBLE
            dialogImgRemove?.visibility = View.VISIBLE
            dialogLayoutPlaceholder?.visibility = View.GONE
            dialogImgSelected?.setImageURI(it)
        }
    }

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

        adapter = CategoryAdapter(
            mutableListOf(),
            onEditClick = { item, _ ->
                if (item.userId == null) {
                    Toast.makeText(this, "Không thể sửa danh mục hệ thống", Toast.LENGTH_SHORT).show()
                } else {
                    showCategoryDialog(item)
                }
            },
            onStatusChange = { item, isChecked ->
                val newStatus = if (isChecked) "ACTIVATE" else "DISABLED"
                controller.updateCategory(this, prefs.authToken, item.id, null, null, newStatus, null)
            }
        )
        
        recyclerCategories.layoutManager = LinearLayoutManager(this)
        recyclerCategories.adapter = adapter
        
        setupSwipeToDelete()

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
            showCategoryDialog(null)
        }
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val item = adapter.getItemAt(position)

                if (item.userId == null) {
                    Toast.makeText(this@CategoryActivity, "Không thể xóa danh mục hệ thống", Toast.LENGTH_SHORT).show()
                    adapter.notifyItemChanged(position)
                    return
                }

                AlertDialog.Builder(this@CategoryActivity)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa danh mục \"${item.name}\"?")
                    .setPositiveButton("Xóa") { _, _ ->
                        controller.deleteCategory(prefs.authToken, item.id)
                    }
                    .setNegativeButton("Hủy") { dialog, _ ->
                        adapter.notifyItemChanged(position)
                        dialog.dismiss()
                    }
                    .setOnCancelListener {
                        adapter.notifyItemChanged(position)
                    }
                    .show()
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerCategories)
    }

    /**
     * Dialog dùng chung cho Thêm mới (item == null) và Chỉnh sửa (item != null)
     */
    private fun showCategoryDialog(categoryItem: CategoryItem?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .create()

        val tvTitle: TextView = dialogView.findViewById(R.id.tvDialogTitle)
        val imgClose: ImageView = dialogView.findViewById(R.id.imgClose)
        val edtName: EditText = dialogView.findViewById(R.id.edtCategoryName)
        val btnPickImage: FrameLayout = dialogView.findViewById(R.id.btnPickImage)
        val btnCancel: View = dialogView.findViewById(R.id.btnCancel)
        val btnSave: View = dialogView.findViewById(R.id.btnSave)
        
        val layoutStatus: LinearLayout = dialogView.findViewById(R.id.layoutStatus)
        val switchStatus: SwitchMaterial = dialogView.findViewById(R.id.switchStatus)
        
        dialogImgSelected = dialogView.findViewById(R.id.imgSelected)
        dialogImgRemove = dialogView.findViewById(R.id.imgRemoveImage)
        dialogLayoutPlaceholder = dialogView.findViewById(R.id.layoutPlaceholder)
        selectedImageUri = null 

        // Fill dữ liệu cũ nếu là EDIT
        if (categoryItem != null) {
            tvTitle.text = "Cập nhật danh mục"
            edtName.setText(categoryItem.name)
            layoutStatus.visibility = View.VISIBLE
            switchStatus.isChecked = categoryItem.isActive
            
            val iconUrl = categoryItem.iconUrl
            if (!iconUrl.isNullOrBlank()) {
                dialogImgSelected?.visibility = View.VISIBLE
                dialogImgRemove?.visibility = View.VISIBLE
                dialogLayoutPlaceholder?.visibility = View.GONE
                Glide.with(this).load("$BASE_URL$iconUrl").into(dialogImgSelected!!)
            }
        }

        imgClose.setOnClickListener { dialog.dismiss() }
        btnCancel.setOnClickListener { dialog.dismiss() }
        
        btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        dialogImgRemove?.setOnClickListener {
            selectedImageUri = null
            dialogImgSelected?.visibility = View.GONE
            dialogImgRemove?.visibility = View.GONE
            dialogLayoutPlaceholder?.visibility = View.VISIBLE
        }

        btnSave.setOnClickListener {
            val name = edtName.text.toString().trim()
            if (name.isEmpty()) {
                edtName.error = "Vui lòng nhập tên danh mục"
                return@setOnClickListener
            }

            val type = if (isExpenseTab) "EXPENSE" else "INCOME"
            val token = prefs.authToken
            
            if (categoryItem == null) {
                // Thêm mới
                controller.addCategory(this, token, name, type, selectedImageUri)
            } else {
                // Cập nhật
                val status = if (switchStatus.isChecked) "ACTIVATE" else "DISABLED"
                controller.updateCategory(this, token, categoryItem.id, name, type, status, selectedImageUri)
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    // ─── CategoryListener callbacks ───────────────────────────────────────────

    override fun onCategoriesLoaded(items: List<CategoryItem>) {
        tvEmptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        recyclerCategories.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        adapter.updateList(items)
    }

    override fun onCategoryAdded(item: CategoryItem) {
        Toast.makeText(this, "Thêm danh mục thành công!", Toast.LENGTH_SHORT).show()
        loadCategories(if (isExpenseTab) "EXPENSE" else "INCOME")
    }

    override fun onCategoryUpdated(item: CategoryItem) {
        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
        loadCategories(if (isExpenseTab) "EXPENSE" else "INCOME")
    }

    override fun onCategoryDeleted(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        loadCategories(if (isExpenseTab) "EXPENSE" else "INCOME")
    }

    override fun onLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        // Nếu lỗi xảy ra (ví dụ 500), reload lại list để revert trạng thái Switch ở UI
        loadCategories(if (isExpenseTab) "EXPENSE" else "INCOME")
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
