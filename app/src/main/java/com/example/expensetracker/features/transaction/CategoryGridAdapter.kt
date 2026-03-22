package com.example.expensetracker.features.transaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.expensetracker.R
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.data.models.CategoryItem

/**
 * CategoryGridAdapter — Adapter cho lưới danh mục trong màn hình Thêm giao dịch.
 */
class CategoryGridAdapter(
    private var categories: MutableList<CategoryItem>,
    private val onCategoryClick: (CategoryItem) -> Unit
) : RecyclerView.Adapter<CategoryGridAdapter.ViewHolder>() {

    private var selectedPosition = -1
    private val BASE_URL = "https://baculine-kelsey-nonethically.ngrok-free.dev"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_grid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        
        val fullIconUrl = if (!category.iconUrl.isNullOrEmpty()) {
            if (category.iconUrl.startsWith("http")) category.iconUrl else ApiClient.BASE_URL + category.iconUrl
        } else null

        Glide.with(holder.itemView.context)
            .load(fullIconUrl)
            .placeholder(R.drawable.ic_logo_spash)
            .error(R.drawable.ic_logo_spash)
            .into(holder.ivIcon)

        holder.tvName.text = category.name
        
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

    fun getSelectedCategory(): CategoryItem? {
        return if (selectedPosition != -1) categories[selectedPosition] else null
    }

    fun setSelectedPosition(position: Int) {
        val prev = selectedPosition
        selectedPosition = position
        notifyItemChanged(prev)
        notifyItemChanged(selectedPosition)
    }

    fun updateList(newItems: List<CategoryItem>) {
        categories.clear()
        categories.addAll(newItems)
        selectedPosition = -1
        notifyDataSetChanged()
    }

    /** Tìm vị trí của category theo ID, trả -1 nếu không tìm thấy */
    fun findPositionByCategoryId(categoryId: Int): Int {
        return categories.indexOfFirst { it.id == categoryId }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivCategoryIcon)
        val tvName: TextView = view.findViewById(R.id.tvCategoryName)
    }
}
