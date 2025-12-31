package com.nubiq.timemanagerapp.utils

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat

object ThemeUtils {

    fun getDividerColor(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.parseColor("#303030") // Dark divider
        } else {
            Color.parseColor("#E0E0E0") // Light divider
        }
    }

    fun isDarkMode(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK

        return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}