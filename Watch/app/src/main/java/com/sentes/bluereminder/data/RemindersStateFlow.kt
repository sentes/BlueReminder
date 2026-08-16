package com.sentes.bluereminder.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object RemindersStateFlow {
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    fun updateReminders(newList: List<Reminder>) {
        _reminders.value = newList.sortedBy { it.reminderTime }
    }

    fun updateSingleReminder(updatedReminder: Reminder) {
        // 1. Get a mutable copy of the current list
        val currentList = _reminders.value.toMutableList()

        // 2. Find the index of the reminder to update
        val index = currentList.indexOfFirst { it.id == updatedReminder.id }

        if (index != -1) {
            // 3a. Update existing reminder
            currentList[index] = updatedReminder
        } else {
            // 3b. Add as new if it doesn't exist (optional behavior)
            currentList.add(updatedReminder)
        }

        // 4. Update the StateFlow with the sorted list
        _reminders.value = currentList
    }
}
