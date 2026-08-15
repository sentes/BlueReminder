package com.sentes.bluereminder.presentation.service

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import org.json.JSONObject

class WatchListenerService : WearableListenerService() {
    override fun onMessageReceived(event: MessageEvent) {
        if (event.path == "/http_response") {
            val json = JSONObject(String(event.data))
            val success = json.getBoolean("success")

            if (success) {
                val code = json.getInt("code")
                val body = json.getString("body")
                PhoneResponseBus.emit(body)
            } else {
                val error = json.optString("error", "Request failed")
                PhoneResponseBus.emit(error)
            }
        }
    }
}