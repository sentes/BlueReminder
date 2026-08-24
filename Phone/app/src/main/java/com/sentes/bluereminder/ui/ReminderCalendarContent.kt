package com.sentes.bluereminder.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sentes.bluereminder.data.Reminder
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import kotlin.collections.component1
import kotlin.collections.component2


@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ReminderCalendarContent(
    reminders: List<Reminder>,
    onToggleReminder: (Reminder) -> Unit,
    onEditReminder: (Reminder) -> Unit,
    onPostponeReminder: (Reminder, Long) -> Unit
) {
    if (reminders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Brak wydarzeń w kalendarzu")
        }
    } else {
        val groupedByDate = remember(reminders) {
            reminders.groupBy {
                Instant.ofEpochMilli(it.eventTime!!)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            groupedByDate.forEach { (date, remindersOnDate) ->
                stickyHeader {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(
                                Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(remindersOnDate, key = { it.id }) { reminder ->
                    ReminderItem(
                        reminder = reminder,
                        onToggle = { onToggleReminder(reminder) },
                        onEdit = { onEditReminder(reminder) },
                        onPostpone = { duration -> onPostponeReminder(reminder, duration) }
                    )
                }
            }
        }
    }
}
