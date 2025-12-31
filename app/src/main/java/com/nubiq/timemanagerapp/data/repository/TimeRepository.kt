package com.nubiq.timemanagerapp.data.repository

import androidx.lifecycle.LiveData
import com.nubiq.timemanagerapp.data.database.TimeActivity
import com.nubiq.timemanagerapp.data.database.dao.TimeActivityDao
import com.nubiq.timemanagerapp.data.database.dao.CategoryDao
import java.text.SimpleDateFormat
import java.util.*

class TimeRepository(
    private val timeActivityDao: TimeActivityDao,
    private val categoryDao: CategoryDao
) {

    fun getAllActivities(): LiveData<List<TimeActivity>> = timeActivityDao.getAllActivities()

    fun getAllCategories(): LiveData<List<com.nubiq.timemanagerapp.data.database.Category>> = categoryDao.getAllCategories()

    suspend fun insertActivity(activity: TimeActivity) {
        timeActivityDao.insert(activity)
    }

    suspend fun updateActivity(activity: TimeActivity) {
        timeActivityDao.update(activity)
    }

    suspend fun deleteActivity(activity: TimeActivity) {
        timeActivityDao.delete(activity)
    }

    fun getActivitiesByDate(date: String): LiveData<List<TimeActivity>> {
        return timeActivityDao.getActivitiesByDate(date)
    }

    fun getAllDates(): LiveData<List<String>> {
        return timeActivityDao.getAllDates()
    }

    suspend fun getDailySummary(date: String): List<TimeActivityDao.ActivitySummary> {
        return try {
            timeActivityDao.getDailySummary(date)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCategoriesList(): List<com.nubiq.timemanagerapp.data.database.Category> {
        val categories = categoryDao.getAllCategories().value ?: emptyList()

        // If no categories exist, create default ones
        if (categories.isEmpty()) {
            createDefaultCategories()
            // Get fresh list after creation
            return categoryDao.getAllCategories().value ?: emptyList()
        }

        return categories
    }

    private suspend fun createDefaultCategories() {
        val defaultCategories = listOf(
            com.nubiq.timemanagerapp.data.database.Category("Work", 0xFF4CAF50.toInt(), "💼", true),
            com.nubiq.timemanagerapp.data.database.Category("Break", 0xFFFF9800.toInt(), "☕", true),
            com.nubiq.timemanagerapp.data.database.Category("Lunch", 0xFFFF5722.toInt(), "🍽️", true),
            com.nubiq.timemanagerapp.data.database.Category("Idle", 0xFF9E9E9E.toInt(), "😴", true),
            com.nubiq.timemanagerapp.data.database.Category("Travel", 0xFF2196F3.toInt(), "🚗", true),
            com.nubiq.timemanagerapp.data.database.Category("Meeting", 0xFF9C27B0.toInt(), "👥", true),
            com.nubiq.timemanagerapp.data.database.Category("Study", 0xFF00BCD4.toInt(), "📚", true),
            com.nubiq.timemanagerapp.data.database.Category("Exercise", 0xFFE91E63.toInt(), "🏃", true)
        )

        defaultCategories.forEach { categoryDao.insert(it) }
    }

    suspend fun addCategory(category: com.nubiq.timemanagerapp.data.database.Category) {
        categoryDao.insert(category)
    }

    suspend fun deleteCategory(category: com.nubiq.timemanagerapp.data.database.Category) {
        categoryDao.delete(category)
    }

    fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}