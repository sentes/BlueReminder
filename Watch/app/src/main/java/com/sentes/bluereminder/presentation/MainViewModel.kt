package com.sentes.bluereminder.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sentes.bluereminder.data.Reminder
import com.sentes.bluereminder.data.RemindersRepository
import com.sentes.bluereminder.service.PhoneService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val phoneService = PhoneService(application)
    
    val reminders: StateFlow<List<Reminder>> = RemindersRepository.reminders

    private val _isLoading = MutableStateFlow(false)
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
}
