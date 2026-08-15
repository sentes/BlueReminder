package com.sentes.bluereminder.service

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

class PhoneListenerService : WearableListenerService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        Log.d(TAG, "listener created")
        super.onCreate()
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "onMessageReceived(): $messageEvent")
        Log.d(TAG, String(messageEvent.data))

        Wearable.getMessageClient(this@PhoneListenerService)
            .sendMessage(
                messageEvent.sourceNodeId, "/today_reminders",
                "test".toByteArray()
            )
            .addOnFailureListener { Log.e("PHONE", "Reply failed", it) }
    }


    override fun onDataChanged(p0: DataEventBuffer) {
        super.onDataChanged(p0)
    }

    companion object {
        private const val TAG = "PhoneListenerService"
    }
}