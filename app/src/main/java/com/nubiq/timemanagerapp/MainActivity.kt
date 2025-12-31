package com.nubiq.timemanagerapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.PieChart
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nubiq.timemanagerapp.data.database.TimeActivity
import com.nubiq.timemanagerapp.data.viewmodel.TimeViewModel
import com.nubiq.timemanagerapp.databinding.ActivityMainBinding
import com.nubiq.timemanagerapp.utils.ChartHelper
import com.nubiq.timemanagerapp.utils.NotificationHelper
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TimeViewModel
    private lateinit var adapter: TimeActivityAdapter
    private lateinit var pieChart: PieChart
    private var selectedDate = ""

    // Activity result launcher for HistoryActivity
    private val historyResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val date = result.data?.getStringExtra("SELECTED_DATE")
            date?.let {
                selectedDate = it
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val displaySdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
                try {
                    val parsedDate = sdf.parse(it)
                    parsedDate?.let { date ->
                        binding.tvDate.text = displaySdf.format(date)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                loadDataForDate(selectedDate)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // This will apply theme
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = TimeViewModel(application)

        // Initialize pie chart
        pieChart = binding.pieChart

        // Setup toolbar
        setSupportActionBar(binding.toolbar)

        // Get today's date
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = sdf.format(Date())
        binding.tvDate.text = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())

        // Setup RecyclerView
        adapter = TimeActivityAdapter(
            onItemClick = { activity ->
                showActivityDetails(activity)
            },
            onEditClick = { activity ->
                editActivity(activity)
            },
            onDeleteClick = { activity ->
                deleteActivity(activity)
            }
        )
        binding.rvActivities.layoutManager = LinearLayoutManager(this)
        binding.rvActivities.adapter = adapter

        // Observe activities for selected date
        viewModel.getActivitiesByDate(selectedDate).observe(this) { activities ->
            activities?.let {
                adapter.submitList(it)
                updateTotalTime(it)
            }
        }

        // Observe daily summary for chart
        viewModel.dailySummary.observe(this) { summary ->
            updateChart(summary)
        }

        // Setup click listeners
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this@MainActivity, AddActivity::class.java)
            intent.putExtra("DATE", selectedDate)
            startActivity(intent)
        }

        binding.btnPrevDay.setOnClickListener {
            changeDate(-1)
        }

        binding.btnNextDay.setOnClickListener {
            changeDate(1)
        }

        binding.btnToday.setOnClickListener {
            selectedDate = sdf.format(Date())
            viewModel.setSelectedDate(selectedDate)
            binding.tvDate.text = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
            loadDataForDate(selectedDate)
        }
    }
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            requestPermissions(arrayOf(permission), 100)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun changeDate(days: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        try {
            calendar.time = sdf.parse(selectedDate) ?: Date()
        } catch (e: Exception) {
            calendar.time = Date()
        }
        calendar.add(Calendar.DAY_OF_YEAR, days)
        selectedDate = sdf.format(calendar.time)
        viewModel.setSelectedDate(selectedDate)
        binding.tvDate.text = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(calendar.time)
        loadDataForDate(selectedDate)
    }

    private fun loadDataForDate(date: String) {
        viewModel.getActivitiesByDate(date).observe(this) { activities ->
            activities?.let {
                adapter.submitList(it)
                updateTotalTime(it)
            }
        }
        // This will trigger the daily summary update
        viewModel.setSelectedDate(date)
    }

    private fun updateChart(summary: List<Pair<String, Float>>) {
        if (summary.isNotEmpty()) {
            // Setup pie chart with data
            ChartHelper.setupPieChart(pieChart, summary, this)

            // Also update the text summary below the chart
            updateTextSummary(summary)
        } else {
            // Clear chart and show message
            pieChart.clear()
            pieChart.setNoDataText("No activities recorded for today")
            pieChart.setNoDataTextColor(android.graphics.Color.GRAY)
            binding.tvChartSummary.text = "📊 Add your first activity using the + button below!"
        }
    }

    private fun updateTextSummary(summary: List<Pair<String, Float>>) {
        val filteredEntries = summary.filter { it.second > 0 }

        if (filteredEntries.isEmpty()) {
            binding.tvChartSummary.text = "No activities recorded"
            return
        }

        val total = filteredEntries.sumOf { it.second.toDouble() }
        if (total == 0.0) {
            binding.tvChartSummary.text = "No duration data available"
            return
        }

        val stringBuilder = StringBuilder()
        stringBuilder.append("📊 Summary: ")

        // Show top 3 categories
        filteredEntries.sortedByDescending { it.second }.take(3).forEachIndexed { index, (label, value) ->
            val percentage = (value.toDouble() / total * 100).toInt()
            if (index > 0) stringBuilder.append(" • ")
            stringBuilder.append("$label ${percentage}%")
        }

        // If more than 3 categories, show count
        if (filteredEntries.size > 3) {
            stringBuilder.append(" • +${filteredEntries.size - 3} more")
        }

        binding.tvChartSummary.text = stringBuilder.toString()
    }

    private fun updateTotalTime(activities: List<TimeActivity>) {
        if (activities.isNotEmpty()) {
            val totalMinutes = activities.sumOf { it.duration }
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            binding.tvTotalTime.text = String.format("Total: %dh %02dm", hours, minutes)
        } else {
            binding.tvTotalTime.text = "Total: 0h 00m"
        }
    }

    private fun showActivityDetails(activity: TimeActivity) {
        // Create a simple dialog showing activity details
        MaterialAlertDialogBuilder(this)
            .setTitle("Activity Details")
            .setMessage(
                """
                Type: ${activity.activityType}
                Time: ${activity.startTime} - ${activity.endTime}
                Duration: ${formatDuration(activity.duration)}
                Notes: ${activity.notes.ifEmpty { "No notes" }}
                """.trimIndent()
            )
            .setPositiveButton("OK", null)
            .setNeutralButton("Edit") { _, _ ->
                editActivity(activity)
            }
            .setNegativeButton("Delete") { _, _ ->
                deleteActivity(activity)
            }
            .show()
    }

    private fun editActivity(activity: TimeActivity) {
        val intent = Intent(this, EditActivity::class.java).apply {
            putExtra("ACTIVITY_ID", activity.id)
            putExtra("DATE", activity.date)
            putExtra("ACTIVITY_TYPE", activity.activityType)
            putExtra("START_TIME", activity.startTime)
            putExtra("END_TIME", activity.endTime)
            putExtra("DURATION", activity.duration)
            putExtra("NOTES", activity.notes)
        }
        startActivity(intent)
    }

    private fun deleteActivity(activity: TimeActivity) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Activity")
            .setMessage("Are you sure you want to delete this activity?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteActivity(activity)
            }
            .setNegativeButton("Cancel", null)
            .show()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_history -> {
                val intent = Intent(this, HistoryActivity::class.java)
                historyResultLauncher.launch(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning from other activities
        loadDataForDate(selectedDate)
    }
}