package com.sentes.bluereminder.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sentes.bluereminder.data.Reminder
import com.sentes.bluereminder.data.RemindersRepository
import com.sentes.bluereminder.service.PhoneService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val phoneService = PhoneService(application)
    
    val reminders: StateFlow<List<Reminder>> = RemindersRepository.reminders

    fun refreshReminders() {
        viewModelScope.launch {
            try {
                phoneService.askForTodayReminders()
            } catch (e: Exception) {
                // Handle error (e.g., log it or show a toast)
            }
        }
    }
}
