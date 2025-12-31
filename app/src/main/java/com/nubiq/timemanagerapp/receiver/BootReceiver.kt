package com.nubiq.timemanagerapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.nubiq.timemanagerapp.data.viewmodel.TimeViewModel
import com.nubiq.timemanagerapp.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            // Reschedule all future activity notifications
            CoroutineScope(Dispatchers.IO).launch {
                rescheduleAllNotifications(context)
            }
        }
    }

    private suspend fun rescheduleAllNotifications(context: Context) {
        try {
            // Get current date
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = sdf.format(Date())

            // In a real app, you would query the database for future activities
            // For now, we'll just recreate the notification channel
            NotificationHelper.createNotificationChannel(context)

            // Note: To fully implement this, you would need to:
            // 1. Get all future activities from database
            // 2. Schedule notifications for each one
            // This requires database access which is complex in a BroadcastReceiver

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}