package com.sentes.bluereminder.service

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import org.json.JSONObject

class WatchListenerService : WearableListenerService() {
    override fun onMessageReceived(event: MessageEvent) {
        if (event.path == "/today_reminders") {
            val json = JSONObject(String(event.data))
            val success = json.getBoolean("success")

            if (success) {
                val code = json.getInt("code")
                val body = json.getString("body")

            } else {
                val error = json.optString("error", "Request failed")
            }
        }
    }
}