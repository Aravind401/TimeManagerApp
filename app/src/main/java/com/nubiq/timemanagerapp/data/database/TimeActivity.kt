package com.nubiq.timemanagerapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "time_activities")
data class TimeActivity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: String, // Format: "yyyy-MM-dd"
    val activityType: String, // e.g., "Work", "Break", "Lunch"
    val startTime: String, // Format: "HH:mm"
    val endTime: String, // Format: "HH:mm"
    val duration: Int, // in minutes
    val notes: String = ""
) {
    fun getFormattedTime(): String {
        return "$startTime - $endTime"
    }
}