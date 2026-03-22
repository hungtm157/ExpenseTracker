package com.example.expensetracker.features.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.expensetracker.R
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.data.models.CategoryItem
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * CategoryAdapter — Hiển thị danh sách CategoryItem từ API.
 */
class CategoryAdapter(
    private var items: MutableList<CategoryItem>,
    private val onEditClick: (CategoryItem, Int) -> Unit,
    private val onStatusChange: (CategoryItem, Boolean) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {



    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconContainer: FrameLayout = view.findViewById(R.id.iconContainer)
        val imgCategoryIcon: ImageView = view.findViewById(R.id.imgCategoryIcon)
        val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)
        val tvCategoryMeta: TextView = view.findViewById(R.id.tvCategoryMeta)
        val switchCategory: SwitchMaterial = view.findViewById(R.id.switchCategory)
        val btnEdit: FrameLayout = view.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = items[position]

        holder.tvCategoryName.text = item.name
        holder.tvCategoryMeta.text = item.metaText

        val iconBg = if (item.type == "INCOME") R.drawable.bg_cat_teal else R.drawable.bg_cat_orange
        holder.iconContainer.setBackgroundResource(iconBg)

        val iconUrl = item.iconUrl
        if (!iconUrl.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load("${ApiClient.BASE_URL}$iconUrl")
                .placeholder(R.drawable.ic_plan)
                .error(R.drawable.ic_logo_spash)
                .into(holder.imgCategoryIcon)
        } else {
            holder.imgCategoryIcon.setImageResource(R.drawable.ic_logo_spash)
        }

        // Switch: bật nếu ACTIVATE. Tắt listener trước khi set để tránh loop
        holder.switchCategory.setOnCheckedChangeListener(null)
        holder.switchCategory.isChecked = item.isActive
        
        // Chỉ cho phép đổi status nếu KHÔNG phải system category (user_id != null)
        // Hoặc cho phép đổi hết? Theo yêu cầu "status sẽ là đổi bằng thanh switch"
        holder.switchCategory.setOnCheckedChangeListener { _, isChecked ->
            onStatusChange(item, isChecked)
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(item, position)
        }
    }

    override fun getItemCount(): Int = items.size

    fun getItemAt(position: Int): CategoryItem = items[position]

    fun updateList(newItems: List<CategoryItem>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }
}
