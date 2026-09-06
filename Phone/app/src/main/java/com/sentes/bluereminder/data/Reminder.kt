package com.sentes.bluereminder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val reminderTime: Long? = null,
    val eventTime: Long? = null,
    val recurrenceType: String = "None",
    val createdAt: Long = System.currentTimeMillis()
)

enum class RecurrenceType(val title: String) {
    None("Brak"),
    Daily("Codziennie"),
    Weekly("Co tydzień"),
    Monthly("Co miesiąc"),
    Yearly("Co rok")
}
