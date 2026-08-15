package com.sentes.bluereminder.service

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.sentes.bluereminder.data.Reminder
import com.sentes.bluereminder.data.RemindersRepository
import org.json.JSONArray
import org.json.JSONException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class WatchListenerService : WearableListenerService() {
    override fun onMessageReceived(event: MessageEvent) {
        Log.d("WatchListenerService", "Message received: ${event.path}")
        if (event.path == "/today_reminders") {
            try {
                val jsonArray = JSONArray(String(event.data))
                val reminders = mutableListOf<Reminder>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    
                    val reminderTime = try {
                        if (obj.has("reminderTime") && !obj.isNull("reminderTime")) {
                            val timeValue = obj.get("reminderTime")
                            when (timeValue) {
                                is Number -> {
                                    Instant.ofEpochMilli(timeValue.toLong())
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime()
                                }
                                is String -> {
                                    // Try to parse as Long first (if stringified number)
                                    val longValue = timeValue.toLongOrNull()
                                    if (longValue != null) {
                                        Instant.ofEpochMilli(longValue)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDateTime()
                                    } else {
                                        LocalDateTime.parse(timeValue)
                                    }
                                }
                                else -> LocalDateTime.now()
                            }
                        } else {
                            LocalDateTime.now()
                        }
                    } catch (e: Exception) {
                        Log.e("WatchListenerService", "Failed to parse time for reminder ${obj.optString("id")}", e)
                        LocalDateTime.now()
                    }

                    reminders.add(
                        Reminder(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            description = obj.getString("description"),
                            reminderTime = reminderTime
                        )
                    )
                }
                RemindersRepository.updateReminders(reminders)
                Log.d("WatchListenerService", "Updated ${reminders.size} reminders")
            } catch (e: JSONException) {
                Log.e("WatchListenerService", "Failed to parse reminders", e)
            }
        }
    }
}
