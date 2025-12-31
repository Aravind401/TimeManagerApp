package com.nubiq.timemanagerapp

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.nubiq.timemanagerapp.data.database.TimeActivity
import com.nubiq.timemanagerapp.data.viewmodel.TimeViewModel
import com.nubiq.timemanagerapp.databinding.ActivityEditBinding
import com.nubiq.timemanagerapp.utils.NotificationHelper
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding
    private lateinit var viewModel: TimeViewModel
    private lateinit var timeActivity: TimeActivity

    private var startHour = 9
    private var startMinute = 0
    private var endHour = 10
    private var endMinute = 0
    private var reminderMinutesBefore = -1 // -1 means no reminder
    private var isFutureDateTime = false
    private var isReminderSectionInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TimeViewModel::class.java]

        // Get activity from intent
        val activityId = intent.getStringExtra("ACTIVITY_ID")
        if (activityId == null) {
            Toast.makeText(this, "Activity not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        setupToolbar()
        loadActivity(activityId)
        checkIfFutureDateTime() // Check first
        setupCategorySpinner()
        setupTimePickers()
        setupReminderSection() // Setup after checking
        setupButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Activity"
    }

    private fun loadActivity(activityId: String) {
        // Get data from intent
        val date = intent.getStringExtra("DATE") ?: getTodayDate()
        val activityType = intent.getStringExtra("ACTIVITY_TYPE") ?: ""
        val startTime = intent.getStringExtra("START_TIME") ?: "09:00"
        val endTime = intent.getStringExtra("END_TIME") ?: "10:00"
        val duration = intent.getIntExtra("DURATION", 60)
        val notes = intent.getStringExtra("NOTES") ?: ""
        val id = activityId

        timeActivity = TimeActivity(
            id = id,
            date = date,
            activityType = activityType,
            startTime = startTime,
            endTime = endTime,
            duration = duration,
            notes = notes
        )

        // Parse start and end times
        val startParts = startTime.split(":")
        val endParts = endTime.split(":")

        if (startParts.size == 2) {
            startHour = startParts[0].toInt()
            startMinute = startParts[1].toInt()
        }

        if (endParts.size == 2) {
            endHour = endParts[0].toInt()
            endMinute = endParts[1].toInt()
        }

        // Set initial values
        binding.etStartTime.text = startTime
        binding.etEndTime.text = endTime
        binding.etNotes.setText(notes)
        binding.tvDuration.text = "Duration: $duration minutes"
    }

    private fun checkIfFutureDateTime() {
        isFutureDateTime = NotificationHelper.isDateTimeInFuture(timeActivity.date, timeActivity.startTime)
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

                // Set current category
                val currentCategoryIndex = categoryNames.indexOf(timeActivity.activityType)
                if (currentCategoryIndex >= 0) {
                    binding.spCategory.setSelection(currentCategoryIndex)
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

            // Set default selection (No reminder)
            binding.spReminder.setSelection(0)

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
            (24 * 60 - startMinutes) + endMinutes
        }
        binding.tvDuration.text = "Duration: $duration minutes"
    }

    private fun setupButtons() {
        binding.btnUpdate.setOnClickListener {
            updateActivity()
        }

        binding.btnDelete.setOnClickListener {
            deleteActivity()
        }
    }

    private fun updateActivity() {
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
            (24 * 60 - startMinutes) + endMinutes
        }

        val updatedActivity = TimeActivity(
            id = timeActivity.id,
            date = timeActivity.date,
            activityType = category,
            startTime = startTime,
            endTime = endTime,
            duration = duration,
            notes = notes
        )

        viewModel.updateActivity(updatedActivity)

        // Cancel existing notification
        NotificationHelper.cancelNotification(this, timeActivity.id)

        // Schedule notification if:
        // 1. It's a future date/time AND
        // 2. User selected a reminder option (reminderMinutesBefore >= 0)
        if (isFutureDateTime && reminderMinutesBefore >= 0) {
            NotificationHelper.scheduleNotification(this, updatedActivity, reminderMinutesBefore)
            Toast.makeText(this, "Reminder updated for $startTime", Toast.LENGTH_SHORT).show()
        }

        Toast.makeText(this, "Activity updated", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun deleteActivity() {
        // Cancel notification before deleting
        NotificationHelper.cancelNotification(this, timeActivity.id)

        viewModel.deleteActivity(timeActivity)
        Toast.makeText(this, "Activity deleted", Toast.LENGTH_SHORT).show()
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