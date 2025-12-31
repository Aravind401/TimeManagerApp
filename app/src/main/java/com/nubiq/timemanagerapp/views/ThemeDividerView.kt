package com.nubiq.timemanagerapp.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.nubiq.timemanagerapp.utils.ThemeManager

class ThemeDividerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    init {
        updateBackgroundColor()
    }

    fun updateBackgroundColor() {
        val dividerColor = if (ThemeManager.isAppDarkMode(context)) {
            android.graphics.Color.parseColor("#303030")
        } else {
            android.graphics.Color.parseColor("#E0E0E0")
        }
        setBackgroundColor(dividerColor)
    }
}