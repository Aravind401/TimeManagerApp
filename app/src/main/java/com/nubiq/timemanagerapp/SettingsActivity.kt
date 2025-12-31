package com.nubiq.timemanagerapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nubiq.timemanagerapp.data.database.Category
import com.nubiq.timemanagerapp.data.viewmodel.TimeViewModel
import com.nubiq.timemanagerapp.databinding.ActivitySettingsBinding
import com.nubiq.timemanagerapp.utils.NotificationHelper
import kotlin.random.Random

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: TimeViewModel

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 100
        private const val THEME_PREF_NAME = "theme_prefs"
        private const val KEY_THEME = "theme_mode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme before creating views
        applySavedTheme()
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TimeViewModel::class.java]

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        setupToolbar()
        setupThemeSettings()
        setupCategoryList()
        setupAddCategoryButton()
        setupNotificationSettings()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
    }

    private fun setupThemeSettings() {
        // Update current theme text
        binding.tvCurrentTheme.text = "Current: ${getCurrentThemeName()}"

        // Theme change button
        binding.btnChangeTheme.setOnClickListener {
            showThemeSelectionDialog()
        }
    }

    private fun showThemeSelectionDialog() {
        val themes = arrayOf("Light Mode", "Dark Mode", "System Default")
        val currentTheme = getSavedTheme()

        var checkedItem = when (currentTheme) {
            "light" -> 0
            "dark" -> 1
            else -> 2 // System Default
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Choose Theme")
            .setSingleChoiceItems(themes, checkedItem) { dialog, which ->
                val selectedTheme = when (which) {
                    0 -> "light"
                    1 -> "dark"
                    else -> "system"
                }

                saveTheme(selectedTheme)
                binding.tvCurrentTheme.text = "Current: ${getCurrentThemeName()}"

                // Apply theme immediately
                applyTheme(selectedTheme)

                // Recreate activity to apply new theme
                recreate()

                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getCurrentThemeName(): String {
        return when (getSavedTheme()) {
            "light" -> "Light Mode"
            "dark" -> "Dark Mode"
            else -> "System Default"
        }
    }

    private fun getSavedTheme(): String {
        val prefs = getSharedPreferences(THEME_PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME, "system") ?: "system"
    }

    private fun saveTheme(theme: String) {
        val prefs = getSharedPreferences(THEME_PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, theme).apply()
    }

    private fun applySavedTheme() {
        val theme = getSavedTheme()
        applyTheme(theme)
    }

    private fun applyTheme(theme: String) {
        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun setupCategoryList() {
        viewModel.allCategories.observe(this) { categories ->
            categories?.let {
                val categoryNames = it.map { it.name }
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    categoryNames
                )
                binding.lvCategories.adapter = adapter

                binding.lvCategories.setOnItemClickListener { _, _, position, _ ->
                    val category = categories[position]
                    showCategoryOptions(category)
                }
            }
        }
    }

    private fun showCategoryOptions(category: Category) {
        if (category.isDefault) {
            MaterialAlertDialogBuilder(this)
                .setTitle(category.name)
                .setMessage("This is a default category and cannot be deleted.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(category.name)
            .setItems(arrayOf("Delete")) { _, which ->
                when (which) {
                    0 -> deleteCategory(category)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteCategory(category: Category) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete '${category.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteCategory(category)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupAddCategoryButton() {
        binding.btnAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun showAddCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val etCategoryName = dialogView.findViewById<EditText>(R.id.etCategoryName)

        MaterialAlertDialogBuilder(this)
            .setTitle("Add New Category")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val categoryName = etCategoryName.text.toString().trim()
                if (categoryName.isNotEmpty()) {
                    val randomColor = Random.nextInt(0x1000000) or 0xFF000000.toInt()
                    val newCategory = Category(
                        name = categoryName,
                        color = randomColor,
                        isDefault = false
                    )
                    viewModel.addCategory(newCategory)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupNotificationSettings() {
        val sharedPrefs = getSharedPreferences("time_tracker_prefs", Context.MODE_PRIVATE)

        // Load saved settings
        val remindersEnabled = sharedPrefs.getBoolean("reminders_enabled", true)
        binding.swReminders.isChecked = remindersEnabled

        // Setup switch listener
        binding.swReminders.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("reminders_enabled", isChecked).apply()

            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (!hasNotificationPermission()) {
                        requestNotificationPermission()
                    }
                }
            }
        }

        // Test notification button
        binding.btnTestNotification.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!hasNotificationPermission()) {
                    requestNotificationPermission()
                    return@setOnClickListener
                }
            }

            NotificationHelper.showTestNotification(
                this,
                "Test Reminder",
                "This is a test notification for Time Tracker"
            )

            Toast.makeText(this, "Test notification sent", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
                    binding.swReminders.isChecked = false
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}