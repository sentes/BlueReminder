package com.sentes.bluereminder.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sentes.bluereminder.data.Reminder
import com.sentes.bluereminder.data.ReminderDatabase
import com.sentes.bluereminder.data.ReminderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReminderRepository
    val reminders: StateFlow<List<Reminder>>

    init {
        val reminderDao = ReminderDatabase.getDatabase(application).reminderDao()
        repository = ReminderRepository(reminderDao)
        reminders = repository.allReminders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun addReminder(title: String, description: String = "", reminderTime: Long? = null, eventTime: Long? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insert(Reminder(title = title, description = description, reminderTime = reminderTime, eventTime = eventTime))
        }
    }

    fun toggleReminderCompletion(reminder: Reminder) {
        viewModelScope.launch {
            repository.update(reminder.copy(isCompleted = !reminder.isCompleted))
        }
    }

    fun updateReminder(reminder: Reminder, title: String, description: String, reminderTime: Long?, eventTime: Long? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.update(reminder.copy(title = title, description = description, reminderTime = reminderTime, eventTime = eventTime))
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.delete(reminder)
        }
    }

    fun postponeReminder(reminder: Reminder) {
        viewModelScope.launch {
            val baseTime = reminder.reminderTime ?: System.currentTimeMillis()
            val newTime = baseTime + (60 * 60 * 1000) // Add 1 hour
            repository.update(reminder.copy(reminderTime = newTime, isCompleted = false))
        }
    }
}
