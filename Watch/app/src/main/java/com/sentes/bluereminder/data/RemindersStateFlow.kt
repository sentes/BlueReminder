package com.sentes.bluereminder.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

object RemindersStateFlow {
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _beingLoadedReminderId = MutableStateFlow<String?>(null)
    val beingLoadedReminderId: StateFlow<String?> = _beingLoadedReminderId.asStateFlow()

    fun updateIsLoading(newIsLoading: Boolean) {
        _isLoading.value = newIsLoading
    }

    fun updateBeingLoadedReminderId(reminderId: String?) {
        _beingLoadedReminderId.value = reminderId
    }

    fun updateReminders(newList: List<Reminder>) {
        _reminders.value = newList.sortedBy { it.reminderTime }
    }

    fun updateSingleReminder(updatedReminder: Reminder) {
        if (_beingLoadedReminderId.value == updatedReminder.id) {
            _beingLoadedReminderId.value = null
        }
        _reminders.update { current ->
            val mutableList = current.toMutableList()
            val index = mutableList.indexOfFirst { it.id == updatedReminder.id }
            if (index != -1) {
                mutableList[index] = updatedReminder
            } else {
                mutableList.add(updatedReminder)
            }

            val today = LocalDate.now()
            if (updatedReminder.reminderTime?.toLocalDate()?.isEqual(today) != true
                && updatedReminder.recurrenceType == "None") {
                mutableList.remove(updatedReminder)
            }

            mutableList
        }
    }

    fun removeReminder(reminderId: String) {
        _reminders.update { current ->
            current.filterNot { it.id == reminderId }
        }
    }
}
