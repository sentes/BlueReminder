package com.sentes.bluereminder.service

import android.content.Context
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.io.InvalidObjectException

class PhoneService(
    private val context: Context,
){

//    suspend fun getPhoneNodeId(): String? {
//        val nodeClient = Wearable.getNodeClient(context)
//        val nodes = nodeClient.connectedNodes.await() // suspends until Task completes
//        return nodes.firstOrNull()?.id
//    }

    suspend fun askForTodayReminders(): String {
        val client = Wearable.getMessageClient(context)

        val capabilityInfo = Wearable.getCapabilityClient(context)
            .getCapability("bluereminder", CapabilityClient.FILTER_REACHABLE).await()

        if (capabilityInfo.nodes.isEmpty()) {
            throw InvalidObjectException("NoPhoneNodesFound")
        }

        val result =
            client.sendMessage(
                capabilityInfo.nodes.first().id,
                "/reminder/get_today",
                "request".toByteArray()
            )
                .await()
        return capabilityInfo.nodes.toString() + "/" + result.toString()
    }

    suspend fun snoozeReminder(reminderId: String): String {
        val client = Wearable.getMessageClient(context)

        val capabilityInfo = Wearable.getCapabilityClient(context)
            .getCapability("bluereminder", CapabilityClient.FILTER_REACHABLE).await()

        if (capabilityInfo.nodes.isEmpty()) {
            throw InvalidObjectException("NoPhoneNodesFound")
        }

        val result =
            client.sendMessage(
                capabilityInfo.nodes.first().id,
                "/reminder/snooze",
                reminderId.toByteArray()
            )
                .await()
        return capabilityInfo.nodes.toString() + "/" + result.toString()
    }

    suspend fun addReminder(title: String, timeMillis: Long): String {
        val client = Wearable.getMessageClient(context)

        val capabilityInfo = Wearable.getCapabilityClient(context)
            .getCapability("bluereminder", CapabilityClient.FILTER_REACHABLE).await()

        if (capabilityInfo.nodes.isEmpty()) {
            throw InvalidObjectException("NoPhoneNodesFound")
        }

        val json = JSONObject().apply {
            put("title", title)
            put("reminderTime", timeMillis)
        }

        val result =
            client.sendMessage(
                capabilityInfo.nodes.first().id,
                "/reminder/add",
                json.toString().toByteArray()
            )
                .await()
        return capabilityInfo.nodes.toString() + "/" + result.toString()
    }

    suspend fun toggleDismissReminder(reminderId: String): String {
        val client = Wearable.getMessageClient(context)

        val capabilityInfo = Wearable.getCapabilityClient(context)
            .getCapability("bluereminder", CapabilityClient.FILTER_REACHABLE).await()

        if (capabilityInfo.nodes.isEmpty()) {
            throw InvalidObjectException("NoPhoneNodesFound")
        }

        val result =
            client.sendMessage(
                capabilityInfo.nodes.first().id,
                "/reminder/toggle_dismiss",
                reminderId.toByteArray()
            )
                .await()
        return capabilityInfo.nodes.toString() + "/" + result.toString()
    }
}
