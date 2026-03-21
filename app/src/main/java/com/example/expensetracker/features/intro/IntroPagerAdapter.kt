package com.example.expensetracker.features.intro

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R

/**
 * IntroPagerAdapter — Adapter cho ViewPager2, render từng slide intro.
 */
class IntroPagerAdapter(
    private val slides: List<SlideModel>
) : RecyclerView.Adapter<IntroPagerAdapter.SlideViewHolder>() {

    inner class SlideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewIconBg: View = itemView.findViewById(R.id.viewIconBg)
        val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_intro_slide, parent, false)
        return SlideViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
        val slide = slides[position]
        val ctx = holder.itemView.context

        holder.viewIconBg.background = ContextCompat.getDrawable(ctx, slide.iconBgRes)
        holder.ivIcon.setImageResource(slide.iconRes)
        holder.tvTitle.text = slide.title
        holder.tvDescription.text = slide.description
    }

    override fun getItemCount(): Int = slides.size
}
