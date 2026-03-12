package com.example.expensetracker.features.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * Adapter hiển thị danh sách danh mục chi tiêu / thu nhập.
 */
class CategoryAdapter(
    private var items: MutableList<CategoryModel>,
    private val onEditClick: (CategoryModel, Int) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconContainer: FrameLayout = view.findViewById(R.id.iconContainer)
        val tvCategoryIcon: TextView = view.findViewById(R.id.tvCategoryIcon)
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

        holder.tvCategoryIcon.text = item.icon
        holder.tvCategoryName.text = item.name
        holder.tvCategoryMeta.text = item.meta
        holder.iconContainer.setBackgroundResource(item.iconBg)

        // Tắt listener trước để tránh trigger khi bind lại
        holder.switchCategory.setOnCheckedChangeListener(null)
        holder.switchCategory.isChecked = item.isEnabled

        holder.switchCategory.setOnCheckedChangeListener { _, isChecked ->
            item.isEnabled = isChecked
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(item, position)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: MutableList<CategoryModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}
