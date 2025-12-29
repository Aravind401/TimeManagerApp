package com.nubiq.timemanagerapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nubiq.timemanagerapp.data.database.TimeActivity
import com.nubiq.timemanagerapp.data.viewmodel.TimeViewModel
import com.nubiq.timemanagerapp.databinding.ActivityMainBinding
import com.nubiq.timemanagerapp.utils.ChartHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TimeViewModel
    private lateinit var adapter: TimeActivityAdapter
    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TimeViewModel::class.java]

        // Setup toolbar
        setSupportActionBar(binding.toolbar)

        // Get today's date
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = sdf.format(Date())
        binding.tvDate.text = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())

        // Setup RecyclerView
        adapter = TimeActivityAdapter { activity ->
            // Handle item click if needed
        }
        binding.rvActivities.layoutManager = LinearLayoutManager(this)
        binding.rvActivities.adapter = adapter

        // Observe activities for selected date
        viewModel.getActivitiesByDate(selectedDate).observe(this) { activities ->
            adapter.submitList(activities)
            updateChart(activities)
            updateTotalTime(activities)
        }

        // Setup click listeners - FIXED HERE
        binding.fabAdd.setOnClickListener {
            // Launch AddActivity with intent
            val intent = android.content.Intent(this@MainActivity, AddActivity::class.java)
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

    private fun changeDate(days: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = sdf.parse(selectedDate) ?: Date()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        selectedDate = sdf.format(calendar.time)
        viewModel.setSelectedDate(selectedDate)
        binding.tvDate.text = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(calendar.time)
        loadDataForDate(selectedDate)
    }

    private fun loadDataForDate(date: String) {
        viewModel.getActivitiesByDate(date).observe(this) { activities ->
            adapter.submitList(activities)
            updateChart(activities)
            updateTotalTime(activities)
        }
    }

    private fun updateChart(activities: List<TimeActivity>) {
        lifecycleScope.launch {
            try {
                val summary = viewModel.getDailySummary(selectedDate)
                val entries = summary.map { Pair(it.activityType, it.totalDuration.toFloat()) }

                // Update text chart
                ChartHelper.updateTextChart(binding.tvChartSummary, entries)

            } catch (e: Exception) {
                binding.tvChartSummary.text = "No chart data available"
            }
        }
    }

    private fun updateTotalTime(activities: List<TimeActivity>) {
        val totalMinutes = activities.sumOf { it.duration }
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        binding.tvTotalTime.text = String.format("Total: %dh %02dm", hours, minutes)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // Start SettingsActivity
                val intent = android.content.Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_history -> {
                // Start HistoryActivity
                val intent = android.content.Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}