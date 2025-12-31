package com.nubiq.timemanagerapp.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.nubiq.timemanagerapp.R
import com.nubiq.timemanagerapp.data.database.TimeActivity
import com.nubiq.timemanagerapp.receiver.NotificationReceiver
import java.text.SimpleDateFormat
import java.util.*

object NotificationHelper {

    const val CHANNEL_ID = "time_tracker_reminders"
    private const val NOTIFICATION_ID = 1001
    private const val REMINDER_REQUEST_CODE = 2001

    // Create notification channel (required for Android 8.0+)
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Schedule notification for an activity
    fun scheduleNotification(context: Context, activity: TimeActivity, reminderMinutesBefore: Int = 15) {
        // Check if reminder is enabled in settings
        val sharedPrefs = context.getSharedPreferences("time_tracker_prefs", Context.MODE_PRIVATE)
        val remindersEnabled = sharedPrefs.getBoolean("reminders_enabled", true)

        if (!remindersEnabled) return

        // Check if user selected "No reminder" (-1 means no reminder)
        if (reminderMinutesBefore < 0) return

        // Parse activity start time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val startTimeStr = "${activity.date} ${activity.startTime}"

        try {
            val startTime = dateFormat.parse(startTimeStr)
            if (startTime != null) {
                // Calculate reminder time
                val calendar = Calendar.getInstance().apply {
                    time = startTime
                    add(Calendar.MINUTE, -reminderMinutesBefore)
                }

                // Check if reminder time is in the future
                if (calendar.timeInMillis > System.currentTimeMillis()) {
                    scheduleExactNotification(context, activity, calendar.timeInMillis)
                } else {
                    // Debug log
                    println("Notification not scheduled: Reminder time is in the past")
                    println("Activity time: $startTimeStr")
                    println("Reminder minutes before: $reminderMinutesBefore")
                    println("Calculated reminder time: ${calendar.time}")
                    println("Current time: ${Date()}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleExactNotification(context: Context, activity: TimeActivity, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("activity_id", activity.id)
            putExtra("activity_type", activity.activityType)
            putExtra("start_time", activity.startTime)
            putExtra("notes", activity.notes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            activity.id.hashCode() + REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }

        // Debug log
        println("Notification scheduled for: ${Date(triggerAtMillis)}")
        println("Activity: ${activity.activityType} at ${activity.startTime}")
    }

    // Cancel scheduled notification
    fun cancelNotification(context: Context, activityId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            activityId.hashCode() + REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    // Show immediate notification (for testing)
    fun showTestNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Use default icon if custom icon doesn't exist
        val iconRes = if (hasNotificationIcon(context)) {
            R.drawable.ic_notification
        } else {
            android.R.drawable.ic_dialog_info
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(500, 500))

        notificationManager.notify(NOTIFICATION_ID, builder.build())
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

    // Check if date/time is in the future (including today's future times)
    fun isDateTimeInFuture(dateStr: String, timeStr: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return try {
            val inputDateTime = sdf.parse("$dateStr $timeStr")
            inputDateTime != null && inputDateTime.time > System.currentTimeMillis()
        } catch (e: Exception) {
            false
        }
    }

    // Check and request notification permission (Android 13+)
    fun checkNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return notificationManager.areNotificationsEnabled()
        }
        return true
    }
}