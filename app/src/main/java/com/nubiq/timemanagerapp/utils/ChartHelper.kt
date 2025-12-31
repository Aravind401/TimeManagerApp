package com.nubiq.timemanagerapp.utils

import android.content.Context
import android.graphics.Color
import android.widget.TextView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.DecimalFormat

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

    // Keep the text chart for backup or alternative
    fun updateTextChart(textView: TextView, entries: List<Pair<String, Float>>) {
        // Filter out zero duration entries
        val filteredEntries = entries.filter { it.second > 0 }

        if (filteredEntries.isEmpty()) {
            textView.text = "📊 No activities recorded for today.\n\nAdd your first activity using the + button below!"
            return
        }

        val total = filteredEntries.sumOf { it.second.toDouble() }
        if (total == 0.0) {
            textView.text = "📊 No duration data available"
            return
        }

        val stringBuilder = StringBuilder()
        stringBuilder.append("📊 Daily Summary:\n\n")

        // Sort by duration (descending)
        filteredEntries.sortedByDescending { it.second }.forEach { (label, value) ->
            val percentage = (value.toDouble() / total * 100).toInt()
            stringBuilder.append("• $label: ${percentage}% (${formatDuration(value.toInt())})\n")
        }

        // Add total time
        val totalMinutes = filteredEntries.sumOf { it.second.toInt() }
        stringBuilder.append("\n⏱️ Total tracked: ${formatDuration(totalMinutes)}")

        textView.text = stringBuilder.toString()
    }

    // New method to setup Pie Chart
    fun setupPieChart(chart: PieChart, entries: List<Pair<String, Float>>, context: Context) {
        // Filter out zero duration entries
        val filteredEntries = entries.filter { it.second > 0 }

        if (filteredEntries.isEmpty()) {
            // Hide chart and show message
            chart.clear()
            chart.setNoDataText("No activities recorded for today")
            chart.setNoDataTextColor(Color.GRAY)
            chart.setNoDataTextTypeface(null)
            return
        }

        // Create pie entries
        val pieEntries = mutableListOf<PieEntry>()
        filteredEntries.forEach { (label, value) ->
            pieEntries.add(PieEntry(value, label))
        }

        // Create dataset
        val dataSet = PieDataSet(pieEntries, "")
        dataSet.colors = getColorsForCategories(filteredEntries.map { it.first })
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE

        // Format values
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return formatDuration(value.toInt())
            }
        }

        // Create pie data
        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(chart))
        pieData.setValueTextSize(11f)
        pieData.setValueTextColor(Color.WHITE)

        // Configure chart appearance
        chart.data = pieData
        chart.setUsePercentValues(true)
        chart.description.isEnabled = false
        chart.setExtraOffsets(5f, 10f, 5f, 5f)
        chart.dragDecelerationFrictionCoef = 0.95f
        chart.isDrawHoleEnabled = true
        chart.setHoleColor(Color.TRANSPARENT)
        chart.transparentCircleRadius = 61f
        chart.setHoleRadius(58f)
        chart.setTransparentCircleColor(Color.WHITE)
        chart.setTransparentCircleAlpha(110)
        chart.rotationAngle = 0f
        chart.isRotationEnabled = true
        chart.isHighlightPerTapEnabled = true
        chart.animateY(1400)
        chart.legend.isEnabled = true
        chart.legend.textSize = 12f
        chart.legend.formSize = 12f
        chart.legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
        chart.legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
        chart.legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
        chart.legend.setDrawInside(false)
        chart.legend.xEntrySpace = 7f
        chart.legend.yEntrySpace = 0f
        chart.legend.yOffset = 0f

        // Center text
        val totalMinutes = filteredEntries.sumOf { it.second.toInt() }
        chart.setCenterText("Total\n${formatDuration(totalMinutes)}")
        chart.setCenterTextSize(14f)
        chart.setCenterTextColor(Color.parseColor("#2196F3"))

        // Entry label styling
        chart.setEntryLabelColor(Color.WHITE)
        chart.setEntryLabelTextSize(12f)

        chart.invalidate() // Refresh chart
    }

    private fun getColorsForCategories(categories: List<String>): List<Int> {
        return categories.map { category ->
            categoryColors[category] ?: getRandomColor()
        }
    }

    private fun getRandomColor(): Int {
        val colors = listOf(
            Color.parseColor("#FF6B6B"),
            Color.parseColor("#4ECDC4"),
            Color.parseColor("#FFD166"),
            Color.parseColor("#06D6A0"),
            Color.parseColor("#118AB2"),
            Color.parseColor("#EF476F"),
            Color.parseColor("#073B4C"),
            Color.parseColor("#7209B7")
        )
        return colors.random()
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
}