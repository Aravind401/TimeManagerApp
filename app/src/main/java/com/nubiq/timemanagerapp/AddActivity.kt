package com.nubiq.timemanagerapp

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.nubiq.timemanagerapp.data.database.TimeActivity
import com.nubiq.timemanagerapp.data.viewmodel.TimeViewModel
import com.nubiq.timemanagerapp.databinding.ActivityAddBinding
import com.nubiq.timemanagerapp.utils.NotificationHelper
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
    private var reminderMinutesBefore = 15 // Default reminder time
    private var isFutureDateTime = false
    private var isReminderSectionInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TimeViewModel::class.java]

        // Get date from intent
        selectedDate = intent.getStringExtra("DATE") ?: getTodayDate()

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        setupToolbar()
        setupCategorySpinner()
        setupTimePickers()
        checkIfFutureDateTime() // Check initially
        setupReminderSection() // Setup after checking
        setupSaveButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Activity"
    }

    private fun setupCategorySpinner() {
        viewModel.allCategories.observe(this) { categories ->
            categories?.let {
                val categoryNames = it.map { category -> category.name }
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    categoryNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spCategory.adapter = adapter

                // Set default selection
                if (categoryNames.isNotEmpty()) {
                    binding.spCategory.setSelection(0)
                }
            }
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
                    checkIfFutureDateTime()
                    updateReminderSectionVisibility()
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

    private fun checkIfFutureDateTime() {
        val startTime = formatTime(startHour, startMinute)
        isFutureDateTime = NotificationHelper.isDateTimeInFuture(selectedDate, startTime)
    }

    private fun setupReminderSection() {
        // Setup reminder time options (only do this once)
        if (!isReminderSectionInitialized) {
            val reminderOptions = arrayOf(
                "No reminder",
                "At start time",
                "5 minutes before",
                "10 minutes before",
                "15 minutes before",
                "30 minutes before",
                "1 hour before",
                "2 hours before"
            )
            val reminderValues = arrayOf(-1, 0, 5, 10, 15, 30, 60, 120)

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                reminderOptions
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spReminder.adapter = adapter

            // Set default selection (15 minutes before)
            binding.spReminder.setSelection(4) // 15 minutes before

            binding.spReminder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    reminderMinutesBefore = reminderValues[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            binding.tvReminderNote.text = "Optional: Get notified before your activity starts"
            isReminderSectionInitialized = true
        }

        // Update visibility based on future check
        updateReminderSectionVisibility()
    }

    private fun updateReminderSectionVisibility() {
        if (isFutureDateTime) {
            // Show reminder section for future activities
            binding.tvReminderTitle.visibility = android.view.View.VISIBLE
            binding.spReminder.visibility = android.view.View.VISIBLE
            binding.tvReminderNote.visibility = android.view.View.VISIBLE
        } else {
            // Hide reminder section for past activities
            binding.tvReminderTitle.visibility = android.view.View.GONE
            binding.spReminder.visibility = android.view.View.GONE
            binding.tvReminderNote.visibility = android.view.View.GONE
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
        val selectedItem = binding.spCategory.selectedItem as? String
        if (selectedItem.isNullOrEmpty()) {
            binding.spCategory.requestFocus()
            return
        }

        val category = selectedItem
        val notes = binding.etNotes.text.toString()
        val startTime = formatTime(startHour, startMinute)
        val endTime = formatTime(endHour, endMinute)
        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute
        val duration = if (endMinutes > startMinutes) {
            endMinutes - startMinutes
        } else {
            (24 * 60 - startMinute) + endMinutes
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

        // Schedule notification if:
        // 1. It's a future date/time AND
        // 2. User selected a reminder option (reminderMinutesBefore >= 0)
        if (isFutureDateTime && reminderMinutesBefore >= 0) {
            NotificationHelper.scheduleNotification(this, activity, reminderMinutesBefore)
            Toast.makeText(this, "Reminder set for $startTime", Toast.LENGTH_SHORT).show()
        }

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