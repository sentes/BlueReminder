package com.sentes.bluereminder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeCard(
    date: Long?,
    hour: Int,
    minute: Int,
    hasTime: Boolean,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onClearClick: () -> Unit,
    onSnoozeClick: (Long) -> Unit
) {
    val dateText = date?.let {
        val selectedDate = Instant.ofEpochMilli(it)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)

        when {
            selectedDate.isEqual(today) -> "Dziś"
            selectedDate.isEqual(tomorrow) -> "Jutro"
            else -> SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(it))
        }
    } ?: "Nie ustawiono"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Data", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row {
                    if (date != null) {
                        TextButton(onClick = onClearClick) {
                            Text("Usuń")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Button(onClick = onDateClick) {
                        Text("Zmień")
                    }
                }
            }

            if (date != null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Godzina", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = if (hasTime) String.format(Locale.getDefault(), "%02d:%02d", hour, minute) else "Nie ustawiono",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SnoozeDropdown(onSnoozeSelected = onSnoozeClick)
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = onTimeClick) {
                            Text("Zmień")
                        }
                    }
                }
            }
        }
    }
}