package com.nubiq.timemanagerapp

import android.app.Application

class TimeManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // No theme initialization needed - MaterialComponents.DayNight handles it
    }
}