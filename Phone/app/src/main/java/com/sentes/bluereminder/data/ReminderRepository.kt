package com.sentes.bluereminder.data

import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {
    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()

    suspend fun getOverdueReminders(currentTime: Long): List<Reminder> {
        return reminderDao.getOverdueReminders(currentTime)
    }

    suspend fun getTodayUnfinishedReminders(endOfDayTimestamp: Long): List<Reminder> {
        return reminderDao.getTodayUnfinishedReminders(endOfDayTimestamp)
    }

    suspend fun insert(reminder: Reminder) {
        reminderDao.insertReminder(reminder)
    }

    suspend fun update(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun delete(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }
}
