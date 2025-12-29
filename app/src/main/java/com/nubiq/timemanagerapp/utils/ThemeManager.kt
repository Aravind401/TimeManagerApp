package com.nubiq.timemanagerapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {

    private const val THEME_PREF_NAME = "theme_preferences"
    private const val THEME_MODE_KEY = "theme_mode"

    fun applyTheme(context: Context) {
        when (getSavedThemeMode(context)) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun saveThemeMode(context: Context, themeMode: String) {
        getPreferences(context).edit().apply {
            putString(THEME_MODE_KEY, themeMode)
            apply()
        }
    }

    fun getSavedThemeMode(context: Context): String {
        return getPreferences(context).getString(THEME_MODE_KEY, "system") ?: "system"
    }

    fun getCurrentThemeText(context: Context): String {
        return when (getSavedThemeMode(context)) {
            "light" -> "Light Mode"
            "dark" -> "Dark Mode"
            else -> "System Default"
        }
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(THEME_PREF_NAME, Context.MODE_PRIVATE)
    }
}