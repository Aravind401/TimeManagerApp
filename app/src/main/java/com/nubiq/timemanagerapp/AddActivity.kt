package com.nubiq.timemanagerapp

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.nubiq.timemanagerapp.data.database.TimeActivity
import com.nubiq.timemanagerapp.data.viewmodel.TimeViewModel
import com.nubiq.timemanagerapp.databinding.ActivityAddBinding
import java.text.SimpleDateFormat
import java.util.*

class AddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBinding
    private lateinit var viewModel: TimeViewModel
    private lateinit var selectedDate: String
    private var startHour = 9
    private var startMinute = 0
    private var endHour = 10
    private var endMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TimeViewModel::class.java]

        // Get date from intent
        selectedDate = intent.getStringExtra("DATE") ?: getTodayDate()

        setupToolbar()
        setupCategorySpinner()
        setupTimePickers()
        setupSaveButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Activity"
    }

    private fun setupCategorySpinner() {
        viewModel.allCategories.observe(this) { categories ->
            val categoryNames = categories.map { it.name }
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                categoryNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spCategory.adapter = adapter
        }
    }

    private fun setupTimePickers() {
        binding.etStartTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    startHour = hour
                    startMinute = minute
                    binding.etStartTime.text = formatTime(hour, minute)
                    calculateDuration()
                },
                startHour,
                startMinute,
                false
            ).show()
        }

        binding.etEndTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    endHour = hour
                    endMinute = minute
                    binding.etEndTime.text = formatTime(hour, minute)
                    calculateDuration()
                },
                endHour,
                endMinute,
                false
            ).show()
        }
    }

    private fun calculateDuration() {
        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute
        val duration = if (endMinutes > startMinutes) {
            endMinutes - startMinutes
        } else {
            (24 * 60 - startMinutes) + endMinutes // Handle overnight
        }
        binding.tvDuration.text = "Duration: $duration minutes"
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            saveActivity()
        }
    }

    private fun saveActivity() {
        val category = binding.spCategory.selectedItem as? String ?: return
        val notes = binding.etNotes.text.toString()

        val startTime = formatTime(startHour, startMinute)
        val endTime = formatTime(endHour, endMinute)
        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute
        val duration = if (endMinutes > startMinutes) {
            endMinutes - startMinutes
        } else {
            (24 * 60 - startMinutes) + endMinutes
        }

        val activity = TimeActivity(
            date = selectedDate,
            activityType = category,
            startTime = startTime,
            endTime = endTime,
            duration = duration,
            notes = notes
        )

        viewModel.insertActivity(activity)
        finish()
    }

    private fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}