package com.sentes.bluereminder.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

object OverdueAlarmScheduler {
    private const val TAG = "OverdueAlarmScheduler"

    fun scheduleNextAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, OverdueReminderReceiver::class.java).apply {
            action = OverdueReminderReceiver.ACTION_CHECK_OVERDUE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextTriggerTime = calculateNext15MinuteMark()

        Log.d(TAG, "Scheduling next exact overdue check for: ${java.util.Date(nextTriggerTime)}")

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextTriggerTime,
                        pendingIntent
                    )
                } else {
                    Log.w(TAG, "Cannot schedule exact alarms, falling back to setAndAllowWhileIdle")
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextTriggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException when scheduling exact alarm", e)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTriggerTime,
                pendingIntent
            )
        }
    }

    private fun calculateNext15MinuteMark(): Long {
        val calendar = Calendar.getInstance()
        val currentMinute = calendar.get(Calendar.MINUTE)
        
        // Find the next 15-minute mark (00, 15, 30, 45)
        val nextMark = when {
            currentMinute < 15 -> 15
            currentMinute < 30 -> 30
            currentMinute < 45 -> 45
            else -> 0
        }

        calendar.set(Calendar.MINUTE, nextMark)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // If we set it to 0, we need to increment the hour
        if (nextMark == 0) {
            calendar.add(Calendar.HOUR_OF_DAY, 1)
        }

        // Safety check: if the calculated time is somehow in the past (e.g., right on the mark), add another 15 minutes
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.MINUTE, 15)
        }

        return calendar.timeInMillis
    }
}
