package com.sentes.bluereminder.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sentes.bluereminder.data.Reminder
import com.sentes.bluereminder.data.RemindersStateFlow
import com.sentes.bluereminder.service.PhoneService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val phoneService = PhoneService(application)
    
    val reminders: StateFlow<List<Reminder>> = RemindersStateFlow.reminders

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun refreshReminders() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                phoneService.askForTodayReminders()
            } catch (e: Exception) {
                // Handle error (e.g., log it or show a toast)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun snoozeReminder(reminder: Reminder) {
        viewModelScope.launch {
            try {
                phoneService.snoozeReminder(reminder.id)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun dismissReminder(reminder: Reminder) {
        RemindersStateFlow.markAsCompleted(reminder.id)
        viewModelScope.launch {
            try {
                phoneService.dismissReminder(reminder.id)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun addQuickReminder(title: String) {
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.HOUR, 1)
                phoneService.addReminder(title, calendar.timeInMillis)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
