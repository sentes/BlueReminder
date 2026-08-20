package com.sentes.bluereminder.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object RemindersStateFlow {
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    fun updateReminders(newList: List<Reminder>) {
        _reminders.value = newList.sortedBy { it.reminderTime }
    }

    fun updateSingleReminder(updatedReminder: Reminder) {
        _reminders.update { current ->
            val mutableList = current.toMutableList()
            val index = mutableList.indexOfFirst { it.id == updatedReminder.id }
            if (index != -1) {
                mutableList[index] = updatedReminder
            } else {
                mutableList.add(updatedReminder)
            }
            mutableList.sortedBy { it.reminderTime }
        }
    }

    fun removeReminder(reminderId: String) {
        _reminders.update { current ->
            current.filterNot { it.id == reminderId }
        }
    }
}
