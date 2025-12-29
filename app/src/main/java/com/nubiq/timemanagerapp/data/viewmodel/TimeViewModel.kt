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

    // Remove the duplicate method, keep only the property
    val allDates: LiveData<List<String>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TimeRepository(db.timeActivityDao(), db.categoryDao())
        allCategories = repository.getAllCategories()
        allDates = repository.getAllDates()  // Initialize allDates

        // Set today's date as default
        _selectedDate.value = repository.getTodayDate()

        // Initialize default categories
        viewModelScope.launch {
            repository.getCategoriesList()
        }
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun getActivitiesByDate(date: String): LiveData<List<TimeActivity>> {
        return repository.getActivitiesByDate(date)
    }

    // Remove the duplicate getAllDates() method

    fun insertActivity(activity: TimeActivity) = viewModelScope.launch {
        repository.insertActivity(activity)
    }

    fun deleteActivity(activity: TimeActivity) = viewModelScope.launch {
        repository.deleteActivity(activity)
    }

    suspend fun getDailySummary(date: String) = repository.getDailySummary(date)

    fun addCategory(category: com.nubiq.timemanagerapp.data.database.Category) = viewModelScope.launch {
        repository.addCategory(category)
    }

    fun deleteCategory(category: com.nubiq.timemanagerapp.data.database.Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }
}