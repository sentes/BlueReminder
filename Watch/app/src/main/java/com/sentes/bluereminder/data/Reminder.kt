package com.sentes.bluereminder.data

import java.time.LocalDateTime

data class Reminder(
    val id: String,
    val title: String,
    val description: String,
    val reminderTime: LocalDateTime?,
    val eventTime: LocalDateTime?,
    val isCompleted: Boolean = false,
    val recurrenceType: String
)
