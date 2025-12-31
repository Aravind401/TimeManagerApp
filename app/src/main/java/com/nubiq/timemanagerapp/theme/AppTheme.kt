package com.nubiq.timemanagerapp.theme

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

object AppTheme {

    private const val PREFS_NAME = "app_theme_prefs"
    private const val KEY_THEME = "theme_mode"

    enum class ThemeMode {
        SYSTEM, LIGHT, DARK
    }

    fun applyTheme(context: Context) {
        val mode = getSavedTheme(context)
        applyTheme(mode)
    }

    fun applyTheme(mode: ThemeMode) {
        when (mode) {
            ThemeMode.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            ThemeMode.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            ThemeMode.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun saveTheme(context: Context, mode: ThemeMode) {
        getPrefs(context).edit().putString(KEY_THEME, mode.name).apply()
        applyTheme(mode)
    }

    fun getSavedTheme(context: Context): ThemeMode {
        val saved = getPrefs(context).getString(KEY_THEME, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(saved ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun isDarkMode(context: Context): Boolean {
        return when (getSavedTheme(context)) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemDarkMode(context)
        }
    }

    private fun isSystemDarkMode(context: Context): Boolean {
        return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}