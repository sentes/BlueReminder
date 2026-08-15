package com.sentes.bluereminder.service

import android.content.Context
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await
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
                "/get_today_reminders",
                "request".toByteArray()
            )
                .await()
        return capabilityInfo.nodes.toString() + "/" + result.toString()
    }
}