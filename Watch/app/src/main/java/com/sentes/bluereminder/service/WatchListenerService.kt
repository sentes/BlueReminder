package com.sentes.bluereminder.service

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.sentes.bluereminder.data.Reminder
import com.sentes.bluereminder.data.RemindersStateFlow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class WatchListenerService : WearableListenerService() {
    override fun onMessageReceived(event: MessageEvent) {
        Log.d("WatchListenerService", "Message received: ${event.path}")
        if (event.path == "/reminder/response_snooze") {
            val jsonObject = JSONObject(String(event.data))
            val reminder = Reminder(
                id = jsonObject.getString("id"),
                title = jsonObject.getString("title"),
                description = jsonObject.getString("description"),
                reminderTime = getTime(jsonObject, "reminderTime"),
                eventTime = getTime(jsonObject, "eventTime"),
                isCompleted = jsonObject.optBoolean("isCompleted", false)
            )

            RemindersStateFlow.updateSingleReminder(reminder)
            Log.d("WatchListenerService", "Updated reminder: $reminder")
        } else if (event.path == "/reminder/response_toggle_dismiss") {
            val jsonObject = JSONObject(String(event.data))
            val reminder = Reminder(
                id = jsonObject.getString("id"),
                title = jsonObject.getString("title"),
                description = jsonObject.getString("description"),
                reminderTime = getTime(jsonObject, "reminderTime"),
                eventTime = getTime(jsonObject, "eventTime"),
                isCompleted = jsonObject.optBoolean("isCompleted", false)
            )

            RemindersStateFlow.updateSingleReminder(reminder)
            Log.d("WatchListenerService", "Updated reminder: $reminder")
        } else if (event.path == "/reminder/response_get_today"
            || event.path == "/reminder/response_add") {
            try {
                val jsonArray = JSONArray(String(event.data))
                val reminders = mutableListOf<Reminder>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)

                    reminders.add(
                        Reminder(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            description = obj.getString("description"),
                            reminderTime = getTime(obj, "reminderTime"),
                            eventTime = getTime(obj, "eventTime"),
                            isCompleted = obj.optBoolean("isCompleted", false)
                        )
                    )
                }
                RemindersStateFlow.updateReminders(reminders)
                Log.d("WatchListenerService", "Updated ${reminders.size} reminders")
            } catch (e: JSONException) {
                Log.e("WatchListenerService", "Failed to parse reminders", e)
            }
        }
    }

    fun getTime(obj: JSONObject, key: String): LocalDateTime? {
        if (obj.has(key) && !obj.isNull(key)) {
            val timeValue = obj.getLong(key)
            return Instant.ofEpochMilli(timeValue)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        }

        return null
    }
}
