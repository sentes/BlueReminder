package com.sentes.bluereminder.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sentes.bluereminder.MainActivity
import com.sentes.bluereminder.data.ReminderDatabase
import com.sentes.bluereminder.data.ReminderRepository
import kotlinx.coroutines.*

class MainForegroundService : Service() {

    private val CHANNEL_ID = "BlueMainForegroundServiceChannel"
    private val OVERDUE_CHANNEL_ID = "BlueOverdueRemindersChannel"
    private val NOTIFICATION_ID_FOREGROUND = 1
    private val NOTIFICATION_ID_OVERDUE = 2

    private lateinit var repository: ReminderRepository
    private var overdueCheckJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        val dao = ReminderDatabase.getDatabase(this).reminderDao()
        repository = ReminderRepository(dao)
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID_FOREGROUND, notification)

        startOverdueCheck()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        overdueCheckJob?.cancel()
        serviceScope.cancel()
    }

    private fun startOverdueCheck() {
        if (overdueCheckJob?.isActive == true) return

        overdueCheckJob = serviceScope.launch {
            while (isActive) {
                checkOverdueReminders()
                delay(15 * 60 * 1000) // 15 minutes
            }
        }
    }

    private suspend fun checkOverdueReminders() {
        val overdueReminders = repository.getOverdueReminders(System.currentTimeMillis())
        val notificationManager = getSystemService(NotificationManager::class.java)

        if (overdueReminders.isNotEmpty()) {
            val contentText = if (overdueReminders.size == 1) {
                "Masz 1 zaległe przypomnienie: ${overdueReminders[0].title}"
            } else {
                "Masz ${overdueReminders.size} zaległych przypomnień"
            }

            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, OVERDUE_CHANNEL_ID)
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

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Blue")
            .setContentText("Blue działa")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Main Foreground Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(serviceChannel)

        val overdueChannel = NotificationChannel(
            OVERDUE_CHANNEL_ID,
            "Overdue Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for overdue reminders"
        }
        manager.createNotificationChannel(overdueChannel)
    }

    companion object {
        fun startService(context: Context) {
            val startIntent = Intent(context, MainForegroundService::class.java)
            context.startForegroundService(startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, MainForegroundService::class.java)
            context.stopService(stopIntent)
        }
    }
}
