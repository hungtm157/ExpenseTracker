package com.example.expensetracker.features.notification

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * NotificationAdapter — Adapter cho RecyclerView hiển thị danh sách thông báo.
 */
class NotificationAdapter(
    private var notifications: List<NotificationModel>,
    private val onItemClick: (NotificationModel) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private val parseDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun updateData(newList: List<NotificationModel>) {
        this.notifications = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivNotificationIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvNotificationMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvNotificationTime)
        private val viewUnread: View = itemView.findViewById(R.id.viewUnreadDot)

        fun bind(notification: NotificationModel) {
            tvTitle.text = notification.title
            tvMessage.text = notification.message

            // Format time
            tvTime.text = try {
                val parsed = parseDateFormat.parse(notification.createdAt.substringBefore("."))
                if (parsed != null) displayDateFormat.format(parsed) else notification.createdAt
            } catch (e: Exception) {
                notification.createdAt
            }

            // Unread dot
            viewUnread.visibility = if (notification.isRead) View.GONE else View.VISIBLE

            // Background tint for unread
            if (!notification.isRead) {
                itemView.setBackgroundColor(Color.parseColor("#F0FFF4"))
            } else {
                itemView.setBackgroundColor(Color.WHITE)
            }

            // Icon based on type
            when (notification.type) {
                "WARNING" -> ivIcon.setImageResource(R.drawable.ic_plan)
                "REMINDER" -> ivIcon.setImageResource(R.drawable.ic_plan)
                else -> ivIcon.setImageResource(R.drawable.ic_plan)
            }

            itemView.setOnClickListener { onItemClick(notification) }
        }
    }
}
