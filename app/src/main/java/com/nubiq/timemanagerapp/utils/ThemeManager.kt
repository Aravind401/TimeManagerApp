package com.nubiq.timemanagerapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {

    private const val THEME_PREF_NAME = "theme_preferences"
    private const val THEME_MODE_KEY = "theme_mode"

    // Theme modes
    const val THEME_MODE_SYSTEM = "system"
    const val THEME_MODE_LIGHT = "light"
    const val THEME_MODE_DARK = "dark"

    /**
     * Apply theme based on saved preference or system default
     */
    fun applyTheme(context: Context) {
        when (getSavedThemeMode(context)) {
            THEME_MODE_LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            THEME_MODE_DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else -> { // SYSTEM or default
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    /**
     * Apply theme to a specific activity (useful for dynamic theme changes)
     */
    fun applyThemeToActivity(activity: AppCompatActivity) {
        activity.delegate.applyDayNight()
    }

    /**
     * Save theme mode preference
     */
    fun saveThemeMode(context: Context, themeMode: String) {
        getPreferences(context).edit().apply {
            putString(THEME_MODE_KEY, themeMode)
            apply()
        }

        // Apply immediately
        when (themeMode) {
            THEME_MODE_LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            THEME_MODE_DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    /**
     * Get saved theme mode
     */
    fun getSavedThemeMode(context: Context): String {
        return getPreferences(context).getString(THEME_MODE_KEY, THEME_MODE_SYSTEM) ?: THEME_MODE_SYSTEM
    }

    /**
     * Get current theme mode text for display
     */
    fun getCurrentThemeText(context: Context): String {
        return when (getSavedThemeMode(context)) {
            THEME_MODE_LIGHT -> "Light Mode"
            THEME_MODE_DARK -> "Dark Mode"
            else -> "System Default"
        }
    }

    /**
     * Check if current system theme is dark mode
     */
    fun isSystemDarkMode(context: Context): Boolean {
        return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }

    /**
     * Check if app is in dark mode (considering both system and app settings)
     */
    fun isAppDarkMode(context: Context): Boolean {
        return when (getSavedThemeMode(context)) {
            THEME_MODE_LIGHT -> false
            THEME_MODE_DARK -> true
            else -> isSystemDarkMode(context) // Follow system
        }
    }

    /**
     * Get the appropriate color based on theme
     */
    fun getColor(context: Context, lightColorRes: Int, darkColorRes: Int): Int {
        return if (isAppDarkMode(context)) {
            android.content.res.Resources.getSystem().getColor(darkColorRes, null)
        } else {
            android.content.res.Resources.getSystem().getColor(lightColorRes, null)
        }
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(THEME_PREF_NAME, Context.MODE_PRIVATE)
    }
}