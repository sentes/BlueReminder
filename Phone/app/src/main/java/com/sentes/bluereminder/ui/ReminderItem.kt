package com.sentes.bluereminder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.sentes.bluereminder.data.Reminder
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@Composable
fun ReminderItem(
    reminder: Reminder,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPostpone: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = onToggle,
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null,
                    color = if (reminder.isCompleted) Color.Gray else Color.Unspecified
                )
                if (reminder.description.isNotBlank()) {
                    Text(
                        text = reminder.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                    )
                }

                Column(modifier = Modifier.padding(top = 4.dp)) {
                    reminder.eventTime?.let { time ->
                        val dateStr = remember(time) {
                            val eventDate = Instant.ofEpochMilli(time)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            val today = LocalDate.now()
                            val tomorrow = today.plusDays(1)

                            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time))

                            when {
                                eventDate.isEqual(today) -> "Dziś $timeStr"
                                eventDate.isEqual(tomorrow) -> "Jutro $timeStr"
                                else -> SimpleDateFormat("dd MMM, yyyy HH:mm", Locale.getDefault()).format(Date(time))
                            }
                        }
                        Text(
                            text = "Termin: $dateStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    reminder.reminderTime?.let { time ->
                        val dateStr = remember(time) {
                            val reminderDate = Instant.ofEpochMilli(time)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            val today = LocalDate.now()
                            val tomorrow = today.plusDays(1)

                            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time))

                            when {
                                reminderDate.isEqual(today) -> "Dziś $timeStr"
                                reminderDate.isEqual(tomorrow) -> "Jutro $timeStr"
                                else -> SimpleDateFormat("dd MMM, yyyy HH:mm", Locale.getDefault()).format(Date(time))
                            }
                        }

                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                SnoozeDropdown(onSnoozeSelected = onPostpone)
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}