package com.nubiq.timemanagerapp.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nubiq.timemanagerapp.data.database.AppDatabase
import com.nubiq.timemanagerapp.data.database.TimeActivity
import com.nubiq.timemanagerapp.data.repository.TimeRepository
import kotlinx.coroutines.launch

class TimeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TimeRepository

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    val allCategories: LiveData<List<com.nubiq.timemanagerapp.data.database.Category>>
    val allDates: LiveData<List<String>>

    // Add LiveData for daily summary
    private val _dailySummary = MutableLiveData<List<Pair<String, Float>>>()
    val dailySummary: LiveData<List<Pair<String, Float>>> = _dailySummary

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TimeRepository(db.timeActivityDao(), db.categoryDao())
        allCategories = repository.getAllCategories()
        allDates = repository.getAllDates()

        // Set today's date as default
        _selectedDate.value = repository.getTodayDate()

        // Initialize default categories
        viewModelScope.launch {
            repository.getCategoriesList()
        }
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        loadDailySummary(date)
    }

    fun getActivitiesByDate(date: String): LiveData<List<TimeActivity>> {
        return repository.getActivitiesByDate(date)
    }

    // Modified method to return LiveData
    fun getDailySummaryLiveData(date: String): LiveData<List<Pair<String, Float>>> {
        loadDailySummary(date)
        return dailySummary
    }

    // Load daily summary in background
    private fun loadDailySummary(date: String) {
        viewModelScope.launch {
            try {
                val summary = repository.getDailySummary(date)
                val entries = summary.map { Pair(it.activityType, it.totalDuration.toFloat()) }
                _dailySummary.postValue(entries)
            } catch (e: Exception) {
                _dailySummary.postValue(emptyList())
            }
        }
    }

    // Keep the suspend function for direct access if needed
    suspend fun getDailySummary(date: String): List<Pair<String, Float>> {
        return try {
            val summary = repository.getDailySummary(date)
            summary.map { Pair(it.activityType, it.totalDuration.toFloat()) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun insertActivity(activity: TimeActivity) = viewModelScope.launch {
        repository.insertActivity(activity)
        // Refresh summary after inserting
        _selectedDate.value?.let { loadDailySummary(it) }
    }

    fun updateActivity(activity: TimeActivity) = viewModelScope.launch {
        repository.updateActivity(activity)
        // Refresh summary after updating
        _selectedDate.value?.let { loadDailySummary(it) }
    }

    fun deleteActivity(activity: TimeActivity) = viewModelScope.launch {
        repository.deleteActivity(activity)
        // Refresh summary after deleting
        _selectedDate.value?.let { loadDailySummary(it) }
    }

    fun addCategory(category: com.nubiq.timemanagerapp.data.database.Category) = viewModelScope.launch {
        repository.addCategory(category)
    }

    fun deleteCategory(category: com.nubiq.timemanagerapp.data.database.Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }
}