package com.sentes.bluereminder.service

import android.app.AlarmManager
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
    private val NOTIFICATION_ID_FOREGROUND = 1

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID_FOREGROUND, notification)

        scheduleOverdueAlarm()

        return START_STICKY
    }

    private fun scheduleOverdueAlarm() {
        OverdueAlarmScheduler.scheduleNextAlarm(this)
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
            OverdueReminderReceiver.OVERDUE_CHANNEL_ID,
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
