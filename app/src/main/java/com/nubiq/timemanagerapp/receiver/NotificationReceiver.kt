package com.nubiq.timemanagerapp.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.nubiq.timemanagerapp.R
import com.nubiq.timemanagerapp.utils.NotificationHelper
import java.text.SimpleDateFormat
import java.util.*

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val activityType = intent.getStringExtra("activity_type") ?: "Activity"
        val startTime = intent.getStringExtra("start_time") ?: ""
        val notes = intent.getStringExtra("notes") ?: ""

        // Create notification
        showNotification(context, activityType, startTime, notes)
    }

    private fun showNotification(context: Context, activityType: String, startTime: String, notes: String) {
        // Ensure notification channel exists
        NotificationHelper.createNotificationChannel(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Use default icon if custom icon doesn't exist
        val iconRes = if (hasNotificationIcon(context)) {
            R.drawable.ic_notification
        } else {
            android.R.drawable.ic_dialog_info
        }

        // Create notification
        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(context.getString(R.string.reminder_message, activityType, startTime))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))

        // Add action to open app
        val mainIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        mainIntent?.let {
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(pendingIntent)
        }

        // Add notes if available
        if (notes.isNotEmpty()) {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(notes))
        }

        // Show notification
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    // Check if notification icon exists
    private fun hasNotificationIcon(context: Context): Boolean {
        return try {
            context.resources.getResourceName(R.drawable.ic_notification)
            true
        } catch (e: Exception) {
            false
        }
    }
}