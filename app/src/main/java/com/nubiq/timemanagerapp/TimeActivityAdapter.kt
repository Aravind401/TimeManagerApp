package com.nubiq.timemanagerapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nubiq.timemanagerapp.data.database.TimeActivity

class TimeActivityAdapter(
    private val onItemClick: (TimeActivity) -> Unit
) : ListAdapter<TimeActivity, TimeActivityAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = getItem(position)
        holder.bind(activity)
        holder.itemView.setOnClickListener { onItemClick(activity) }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvActivityType: TextView = itemView.findViewById(R.id.tvActivityType)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val tvNotes: TextView = itemView.findViewById(R.id.tvNotes)

        fun bind(activity: TimeActivity) {
            tvActivityType.text = activity.activityType
            tvTime.text = activity.getFormattedTime()
            tvDuration.text = formatDuration(activity.duration)
            tvNotes.text = activity.notes
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