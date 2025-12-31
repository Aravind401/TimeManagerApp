package com.nubiq.timemanagerapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

object SimpleThemeManager {

    private const val PREFS_NAME = "app_theme"
    private const val KEY_THEME = "theme"

    fun applySavedTheme(context: Context) {
        val theme = getSavedTheme(context)
        applyTheme(theme)
    }

    fun applyTheme(theme: String) {
        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun saveTheme(context: Context, theme: String) {
        getPrefs(context).edit().putString(KEY_THEME, theme).apply()
        applyTheme(theme)
    }

    fun getSavedTheme(context: Context): String {
        return getPrefs(context).getString(KEY_THEME, "system") ?: "system"
    }

    fun getThemeDisplayName(context: Context): String {
        return when (getSavedTheme(context)) {
            "light" -> "Light Mode"
            "dark" -> "Dark Mode"
            else -> "System Default"
        }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}