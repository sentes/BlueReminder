package com.sentes.bluereminder.service

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
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

        if (messageEvent.path == "/get_today_reminders") {
            serviceScope.launch {
                try {
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
                            put("reminderTime", reminder.reminderTime)
                        }
                        jsonArray.put(jsonObject)
                    }
                    
                    val responseText = jsonArray.toString()

                    Wearable.getMessageClient(this@PhoneListenerService)
                        .sendMessage(
                            messageEvent.sourceNodeId, "/today_reminders",
                            responseText.toByteArray()
                        )
                        .addOnSuccessListener { Log.d(TAG, "Reminders sent to wearable") }
                        .addOnFailureListener { Log.e(TAG, "Reply failed", it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching reminders", e)
                }
            }
        }
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