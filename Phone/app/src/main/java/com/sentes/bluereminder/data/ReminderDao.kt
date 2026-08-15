package com.sentes.bluereminder.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, reminderTime IS NULL ASC, reminderTime ASC, createdAt DESC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 AND reminderTime IS NOT NULL AND reminderTime < :currentTime")
    suspend fun getOverdueReminders(currentTime: Long): List<Reminder>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 AND reminderTime IS NOT NULL AND reminderTime < :endOfDayTimestamp")
    suspend fun getTodayUnfinishedReminders(endOfDayTimestamp: Long): List<Reminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)
}
