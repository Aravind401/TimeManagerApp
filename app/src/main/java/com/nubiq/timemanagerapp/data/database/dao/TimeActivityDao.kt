package com.nubiq.timemanagerapp.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.nubiq.timemanagerapp.data.database.TimeActivity

@Dao
interface TimeActivityDao {

    @Insert
    suspend fun insert(activity: TimeActivity)

    @Update
    suspend fun update(activity: TimeActivity)

    @Delete
    suspend fun delete(activity: TimeActivity)

    @Query("SELECT * FROM time_activities WHERE date = :date ORDER BY startTime")
    fun getActivitiesByDate(date: String): LiveData<List<TimeActivity>>

    @Query("SELECT * FROM time_activities ORDER BY date DESC, startTime")
    fun getAllActivities(): LiveData<List<TimeActivity>>

    // Make sure this method exists
    @Query("SELECT DISTINCT date FROM time_activities ORDER BY date DESC")
    fun getAllDates(): LiveData<List<String>>

    @Query("SELECT * FROM time_activities WHERE date = :date AND activityType = :type")
    suspend fun getActivityByDateAndType(date: String, type: String): List<TimeActivity>

    data class ActivitySummary(
        val activityType: String,
        val totalDuration: Int
    )

    @Query("SELECT activityType, SUM(duration) as totalDuration FROM time_activities WHERE date = :date GROUP BY activityType")
    suspend fun getDailySummary(date: String): List<ActivitySummary>
}