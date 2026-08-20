package com.sentes.bluereminder.service

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.sentes.bluereminder.data.Reminder
import com.sentes.bluereminder.data.ReminderDatabase
import com.sentes.bluereminder.data.ReminderRepository
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class PhoneListenerService : WearableListenerService() {

    private lateinit var repository: ReminderRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        Log.d(TAG, "listener created")
        super.onCreate()
        val dao = ReminderDatabase.getDatabase(this).reminderDao()
        repository = ReminderRepository(dao)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "onMessageReceived(): ${messageEvent.path}")

        if (messageEvent.path == "/reminder/add") {
            serviceScope.launch {
                try {
                    val reminderJson = String(messageEvent.data, Charsets.UTF_8)
                    val reminderObject = JSONObject(reminderJson)
                    val title = reminderObject.getString("title")
                    val reminderTime = if (reminderObject.has("reminderTime")) reminderObject.getLong("reminderTime") else null
                    
                    val reminder = Reminder(
                        title = title,
                        reminderTime = reminderTime
                    )
                    repository.insert(reminder)
                    Log.d(TAG, "Reminder added from wearable: $title")

                    Wearable.getMessageClient(this@PhoneListenerService)
                        .sendMessage(
                            messageEvent.sourceNodeId, "/reminder/response_add",
                            getTodayRemindersResponseText().toByteArray()
                        )
                        .addOnSuccessListener { Log.d(TAG, "Reminders sent to wearable") }
                        .addOnFailureListener { Log.e(TAG, "Reply failed", it) }
                }
                catch (e: Exception) {
                    Log.e(TAG, "Error parsing or inserting reminder JSON", e)
                }
            }

        } else if (messageEvent.path == "/reminder/snooze") {
            serviceScope.launch {
                try {
                    val requestJson = String(messageEvent.data, Charsets.UTF_8)
                    val requestObject = JSONObject(requestJson)

                    val reminderId = requestObject.getString("id").toLongOrNull()
                    val durationHours = requestObject.getInt("durationHours")
                    if (reminderId != null) {
                        val reminder = repository.getReminderById(reminderId)
                        if (reminder != null) {
                            val baseTime = reminder.reminderTime ?: System.currentTimeMillis()
                            val newTime = baseTime + (60 * 60 * 1000) * durationHours
                            val updatedReminder = reminder.copy(reminderTime = newTime, isCompleted = false)
                            repository.update(updatedReminder)
                            Log.d(TAG, "Reminder $reminderId snoozed by $durationHours hours")

                            // Send updated reminder back to wearable
                            val jsonObject = JSONObject().apply {
                                put("id", updatedReminder.id)
                                put("title", updatedReminder.title)
                                put("description", updatedReminder.description)
                                put("reminderTime", updatedReminder.reminderTime)
                                put("eventTime", updatedReminder.eventTime)
                            }
                            
                            Wearable.getMessageClient(this@PhoneListenerService)
                                .sendMessage(
                                    messageEvent.sourceNodeId, "/reminder/response_snooze",
                                    jsonObject.toString().toByteArray()
                                )
                                .addOnSuccessListener { Log.d(TAG, "Updated reminder sent to wearable") }
                                .addOnFailureListener { Log.e(TAG, "Failed to send snooze response", it) }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error snoozing reminder", e)
                }
            }
        }
        else if (messageEvent.path == "/reminder/toggle_dismiss") {
            serviceScope.launch {
                try {
                    val reminderId = String(messageEvent.data, Charsets.UTF_8).toLongOrNull()
                    if (reminderId != null) {
                        val reminder = repository.getReminderById(reminderId)
                        if (reminder != null) {
                            val updatedReminder = reminder.copy(isCompleted = !reminder.isCompleted)
                            repository.update(updatedReminder)
                            Log.d(TAG, "Reminder $reminderId completed")


                            val jsonObject = JSONObject().apply {
                                put("id", updatedReminder.id)
                                put("title", updatedReminder.title)
                                put("description", updatedReminder.description)
                                put("reminderTime", updatedReminder.reminderTime)
                                put("eventTime", updatedReminder.eventTime)
                                put("isCompleted", updatedReminder.isCompleted)
                            }

                            Wearable.getMessageClient(this@PhoneListenerService)
                                .sendMessage(
                                    messageEvent.sourceNodeId, "/reminder/response_toggle_dismiss",
                                    jsonObject.toString().toByteArray()
                                )
                                .addOnSuccessListener { Log.d(TAG, "Updated reminder sent to wearable") }
                                .addOnFailureListener { Log.e(TAG, "Failed to send snooze response", it) }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error snoozing reminder", e)
                }
            }
        }
        else if (messageEvent.path == "/reminder/get_today") {
            serviceScope.launch {
                try {
                    Wearable.getMessageClient(this@PhoneListenerService)
                        .sendMessage(
                            messageEvent.sourceNodeId, "/reminder/response_get_today",
                            getTodayRemindersResponseText().toByteArray()
                        )
                        .addOnSuccessListener { Log.d(TAG, "Reminders sent to wearable") }
                        .addOnFailureListener { Log.e(TAG, "Reply failed", it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching reminders", e)
                }
            }
        }
    }

    private suspend fun getTodayRemindersResponseText(): String {
        val endOfDay = LocalDate.now().atTime(LocalTime.MAX)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val reminders = repository.getTodayUnfinishedReminders(endOfDay)

        val jsonArray = JSONArray()
        reminders.forEach { reminder ->
            val jsonObject = JSONObject().apply {
                put("id", reminder.id)
                put("title", reminder.title)
                put("description", reminder.description)
                put("eventTime", reminder.eventTime)
                put("reminderTime", reminder.reminderTime)
            }
            jsonArray.put(jsonObject)
        }

        return jsonArray.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }


    override fun onDataChanged(p0: DataEventBuffer) {
        super.onDataChanged(p0)
    }

    companion object {
        private const val TAG = "PhoneListenerService"
    }
}