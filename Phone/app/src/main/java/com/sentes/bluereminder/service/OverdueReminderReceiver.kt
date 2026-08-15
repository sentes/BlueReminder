package com.sentes.bluereminder.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.sentes.bluereminder.MainActivity
import com.sentes.bluereminder.data.ReminderDatabase
import com.sentes.bluereminder.data.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class OverdueReminderReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_CHECK_OVERDUE) {
            val pendingResult = goAsync()
            
            // Reschedule the next alarm immediately
            OverdueAlarmScheduler.scheduleNextAlarm(context)

            receiverScope.launch {
                try {
                    checkOverdueReminders(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private suspend fun checkOverdueReminders(context: Context) {
        val dao = ReminderDatabase.getDatabase(context).reminderDao()
        val repository = ReminderRepository(dao)
        val overdueReminders = repository.getOverdueReminders(System.currentTimeMillis())
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (overdueReminders.isNotEmpty()) {
            val contentText = if (overdueReminders.size == 1) {
                "Masz 1 zaległe przypomnienie: ${overdueReminders[0].title}"
            } else {
                "Masz ${overdueReminders.size} zaległych przypomnień"
            }

            val mainIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, OVERDUE_CHANNEL_ID)
                .setContentTitle("Zaległe przypomnienia")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(NOTIFICATION_ID_OVERDUE, notification)
        } else {
            notificationManager.cancel(NOTIFICATION_ID_OVERDUE)
        }
    }

    companion object {
        const val ACTION_CHECK_OVERDUE = "com.sentes.bluereminder.ACTION_CHECK_OVERDUE"
        const val OVERDUE_CHANNEL_ID = "BlueOverdueRemindersChannel"
        const val NOTIFICATION_ID_OVERDUE = 2
    }
}
