package com.nubiq.timemanagerapp

import android.graphics.Color
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.util.TimeUtils.formatDuration
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nubiq.timemanagerapp.data.database.TimeActivity

class TimeActivityAdapter(
    private val onItemClick: (TimeActivity) -> Unit,
    private val onEditClick: (TimeActivity) -> Unit,
    private val onDeleteClick: (TimeActivity) -> Unit
) : ListAdapter<TimeActivity, TimeActivityAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = getItem(position)
        holder.bind(activity)

        // Set long click listener for context menu
        holder.itemView.setOnLongClickListener {
            showPopupMenu(it, activity)
            true
        }

        // Regular click for viewing details
        holder.itemView.setOnClickListener {
            onItemClick(activity)
        }
    }

    private fun showPopupMenu(view: View, activity: TimeActivity) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.activity_context_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit -> {
                    onEditClick(activity)
                    true
                }
                R.id.menu_delete -> {
                    onDeleteClick(activity)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvActivityType: TextView = itemView.findViewById(R.id.tvActivityType)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val tvNotes: TextView = itemView.findViewById(R.id.tvNotes)
        private val divider: View = itemView.findViewById(R.id.divider) // Add ID to divider view

        fun bind(activity: TimeActivity) {
            tvActivityType.text = activity.activityType
            tvTime.text = activity.getFormattedTime()
            tvDuration.text = formatDuration(activity.duration)
            tvNotes.text = activity.notes.ifEmpty { "No notes" }

            // Update divider color based on theme
            updateDividerColor()
        }

        private fun updateDividerColor() {
            val context = itemView.context
            val isDarkMode = (context.resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES

            divider.setBackgroundColor(
                if (isDarkMode) Color.parseColor("#303030")
                else Color.parseColor("#E0E0E0")
            )
        }


        private fun formatDuration(minutes: Int): String {
            return if (minutes < 60) {
                "${minutes}m"
            } else {
                val hours = minutes / 60
                val mins = minutes % 60
                "${hours}h ${mins}m"
            }
        }

        // ... rest of your code
    }


    class DiffCallback : DiffUtil.ItemCallback<TimeActivity>() {
        override fun areItemsTheSame(oldItem: TimeActivity, newItem: TimeActivity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TimeActivity, newItem: TimeActivity): Boolean {
            return oldItem == newItem
        }
    }
}