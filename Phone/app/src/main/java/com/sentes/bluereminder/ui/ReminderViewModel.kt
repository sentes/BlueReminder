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
import org.json.JSONArray
import org.json.JSONObject

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

    fun postponeReminder(reminder: Reminder, durationMillis: Long) {
        viewModelScope.launch {
            val baseTime = reminder.reminderTime ?: System.currentTimeMillis()
            val newTime = baseTime + durationMillis
            repository.update(reminder.copy(reminderTime = newTime, isCompleted = false))
        }
    }

    fun getRemindersJson(): String {
        val currentReminders = reminders.value
        val jsonArray = JSONArray()
        currentReminders.forEach { reminder ->
            val jsonObject = JSONObject().apply {
                put("title", reminder.title)
                put("description", reminder.description)
                put("isCompleted", reminder.isCompleted)
                put("reminderTime", reminder.reminderTime ?: JSONObject.NULL)
                put("eventTime", reminder.eventTime ?: JSONObject.NULL)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString(4)
    }

    fun importRemindersFromJson(jsonString: String) {
        viewModelScope.launch {
            try {
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val reminder = Reminder(
                        title = jsonObject.getString("title"),
                        description = jsonObject.optString("description", ""),
                        isCompleted = jsonObject.optBoolean("isCompleted", false),
                        reminderTime = if (jsonObject.isNull("reminderTime")) null else jsonObject.getLong("reminderTime"),
                        eventTime = if (jsonObject.isNull("eventTime")) null else jsonObject.getLong("eventTime")
                    )
                    repository.insert(reminder)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
