package com.sentes.bluereminder.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sentes.bluereminder.ReminderGroup
import com.sentes.bluereminder.data.Reminder
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach


@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ReminderListContent(
    groupedReminders: Map<ReminderGroup, List<Reminder>>,
    currentTime: Long,
    onToggleReminder: (Reminder) -> Unit,
    onEditReminder: (Reminder) -> Unit,
    onPostponeReminder: (Reminder, Long) -> Unit
) {
    if (groupedReminders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Brak powiadomień")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            groupedReminders.forEach { (group, remindersInGroup) ->
                stickyHeader {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = group.title,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(remindersInGroup, key = { it.id }) { reminder ->
                    ReminderItem(
                        reminder = reminder,
                        currentTime = currentTime,
                        onToggle = { onToggleReminder(reminder) },
                        onEdit = { onEditReminder(reminder) },
                        onPostpone = { duration -> onPostponeReminder(reminder, duration) }
                    )
                }
            }
        }
    }
}
