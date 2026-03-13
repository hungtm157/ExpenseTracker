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
import com.example.expensetracker.data.models.CategoryItem
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * CategoryAdapter — Hiển thị danh sách CategoryItem từ API.
 * Load icon từ icon_url (baseUrl + path). Fallback ic_plan nếu null.
 * Switch phản ánh status: ACTIVATE = bật, DEACTIVATE = tắt.
 */
class CategoryAdapter(
    private var items: MutableList<CategoryItem>,
    private val onEditClick: (CategoryItem, Int) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    companion object {
        private const val BASE_URL = "https://maddie-conditioned-increasingly.ngrok-free.dev"
    }

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

        // Tên và meta text
        holder.tvCategoryName.text = item.name
        holder.tvCategoryMeta.text = item.metaText

        // Màu nền icon container theo type
        val iconBg = if (item.type == "INCOME") R.drawable.bg_cat_teal else R.drawable.bg_cat_orange
        holder.iconContainer.setBackgroundResource(iconBg)

        // Load icon từ icon_url → ghép BASE_URL + path, fallback ic_plan nếu null
        val iconUrl = item.iconUrl
        if (!iconUrl.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load("$BASE_URL$iconUrl")
                .placeholder(R.drawable.ic_plan)
                .error(R.drawable.ic_plan)
                .into(holder.imgCategoryIcon)
        } else {
            holder.imgCategoryIcon.setImageResource(R.drawable.ic_logo_spash)
        }

        // Switch: bật nếu ACTIVATE
        holder.switchCategory.setOnCheckedChangeListener(null)
        holder.switchCategory.isChecked = item.isActive
        holder.switchCategory.setOnCheckedChangeListener { _, _ -> }

        holder.btnEdit.setOnClickListener {
            onEditClick(item, position)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<CategoryItem>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }
}
