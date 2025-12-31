package com.nubiq.timemanagerapp

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

open class BaseActivity : AppCompatActivity() {

    companion object {
        private const val THEME_PREF_NAME = "theme_prefs"
        private const val KEY_THEME = "theme_mode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme before creating views
        applySavedTheme()
        super.onCreate(savedInstanceState)
    }

    protected fun getSavedTheme(): String {
        val prefs = getSharedPreferences(THEME_PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME, "system") ?: "system"
    }

    protected fun applySavedTheme() {
        val theme = getSavedTheme()
        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}