package com.nubiq.timemanagerapp.utils

import android.content.Context
import android.graphics.Color
import android.widget.TextView

object ChartHelper {

    private val categoryColors = mapOf(
        "Work" to Color.parseColor("#4CAF50"),
        "Break" to Color.parseColor("#FF9800"),
        "Lunch" to Color.parseColor("#FF5722"),
        "Idle" to Color.parseColor("#9E9E9E"),
        "Travel" to Color.parseColor("#2196F3"),
        "Meeting" to Color.parseColor("#9C27B0"),
        "Study" to Color.parseColor("#00BCD4"),
        "Exercise" to Color.parseColor("#E91E63")
    )

    fun updateTextChart(textView: TextView, entries: List<Pair<String, Float>>) {
        val total = entries.sumOf { it.second.toDouble() }
        if (total == 0.0) {
            textView.text = "No data available for today"
            return
        }

        val stringBuilder = StringBuilder()
        stringBuilder.append("📊 Daily Summary:\n\n")

        // Sort by duration (descending)
        entries.sortedByDescending { it.second }.forEach { (label, value) ->
            if (value > 0) {
                val percentage = (value.toDouble() / total * 100).toInt()
                stringBuilder.append("• $label: ${percentage}% (${formatDuration(value.toInt())})\n")
            }
        }

        // Add total time
        val totalMinutes = entries.sumOf { it.second.toInt() }
        stringBuilder.append("\n⏱️ Total tracked: ${formatDuration(totalMinutes)}")

        textView.text = stringBuilder.toString()
    }

    private fun formatDuration(minutes: Int): String {
        return if (minutes < 60) {
            "${minutes}m"
        } else {
            val hours = minutes / 60
            val mins = minutes % 60
            if (mins == 0) {
                "${hours}h"
            } else {
                "${hours}h ${mins}m"
            }
        }
    }

    fun getColorForCategory(category: String): Int {
        return categoryColors[category] ?: getRandomColor()
    }

    private fun getRandomColor(): Int {
        return Color.rgb(
            (0..255).random(),
            (0..255).random(),
            (0..255).random()
        )
    }
}