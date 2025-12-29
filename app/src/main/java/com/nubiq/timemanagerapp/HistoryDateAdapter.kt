package com.nubiq.timemanagerapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class HistoryDateAdapter(
    private val onDateClick: (String) -> Unit
) : ListAdapter<String, HistoryDateAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_date, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date = getItem(position)
        holder.bind(date)
        holder.itemView.setOnClickListener { onDateClick(date) }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvDay: TextView = itemView.findViewById(R.id.tvDay)

        fun bind(date: String) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val displaySdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
            val parsedDate = sdf.parse(date) ?: Date()

            tvDate.text = displaySdf.format(parsedDate)
            tvDay.text = SimpleDateFormat("EEEE", Locale.getDefault()).format(parsedDate)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}