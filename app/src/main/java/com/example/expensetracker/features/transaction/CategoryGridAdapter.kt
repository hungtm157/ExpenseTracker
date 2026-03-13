package com.example.expensetracker.features.transaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.features.category.CategoryModel

/**
 * CategoryGridAdapter — Adapter cho lưới danh mục trong màn hình Thêm giao dịch.
 */
class CategoryGridAdapter(
    private val categories: List<CategoryModel>,
    private val onCategoryClick: (CategoryModel) -> Unit
) : RecyclerView.Adapter<CategoryGridAdapter.ViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_grid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.tvIcon.text = category.icon
        holder.tvName.text = category.name
        
        // Highlight selection using the selector in XML
        holder.itemView.isSelected = (position == selectedPosition)

        holder.itemView.setOnClickListener {
            val prev = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(prev)
            notifyItemChanged(selectedPosition)
            onCategoryClick(category)
        }
    }

    override fun getItemCount(): Int = categories.size

    fun getSelectedCategory(): CategoryModel? {
        return if (selectedPosition != -1) categories[selectedPosition] else null
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcon: TextView = view.findViewById(R.id.ivCategoryIcon)
        val tvName: TextView = view.findViewById(R.id.tvCategoryName)
    }
}
