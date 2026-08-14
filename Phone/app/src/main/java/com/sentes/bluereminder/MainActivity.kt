package com.sentes.bluereminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sentes.bluereminder.data.Reminder
import com.sentes.bluereminder.ui.ReminderViewModel
import com.sentes.bluereminder.ui.theme.BlueReminderTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlueReminderTheme {
                ReminderApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderApp(viewModel: ReminderViewModel = viewModel()) {
    val reminders by viewModel.reminders.collectAsState()
    var showAddDialog by remember { mutableStateOf(value = false) }
    var editingReminder by remember { mutableStateOf<Reminder?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Blue Reminders") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (reminders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No reminders yet. Tap + to add one!")
                }
            } else {
                LazyColumn {
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderItem(
                            reminder = reminder,
                            onToggle = { viewModel.toggleReminderCompletion(reminder) },
                            onEdit = { editingReminder = reminder },
                            onDelete = { viewModel.deleteReminder(reminder) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            ReminderDialog(
                onDismiss = { showAddDialog = false }
            ) { title, desc, time ->
                viewModel.addReminder(title, desc, time)
                showAddDialog = false
            }
        }

        editingReminder?.let { reminder ->
            ReminderDialog(
                reminder = reminder,
                onDismiss = { editingReminder = null }
            ) { title, desc, time ->
                viewModel.updateReminder(reminder, title, desc, time)
                editingReminder = null
            }
        }
    }
}

@Composable
fun ReminderItem(
    reminder: Reminder,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
                reminder.reminderTime?.let { time ->
                    val dateStr = remember(time) {
                        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(time))
                    }
                    Text(
                        text = "Scheduled: $dateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Row {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDialog(
    reminder: Reminder? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long?) -> Unit
) {
    val calendar = remember(reminder) {
        Calendar.getInstance().apply {
            if (reminder?.reminderTime != null) {
                timeInMillis = reminder.reminderTime
            } else {
                add(Calendar.HOUR_OF_DAY, 1)
            }
        }
    }
    
    val initialDate = remember(reminder) {
        Calendar.getInstance().apply {
            if (reminder?.reminderTime != null) {
                timeInMillis = reminder.reminderTime
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    var title by remember { mutableStateOf(reminder?.title ?: "") }
    var description by remember { mutableStateOf(reminder?.description ?: "") }
    var selectedDate by remember { mutableStateOf<Long?>(initialDate) }
    var selectedHour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }
    var hasTime by remember { mutableStateOf(reminder?.reminderTime != null || reminder == null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate)
    val timePickerState = rememberTimePickerState(
        initialHour = selectedHour,
        initialMinute = selectedMinute
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (reminder == null) "Add Reminder" else "Edit Reminder") },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedDate?.let {
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                        } ?: "No date set",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = { showDatePicker = true }) {
                        Text("Set Date")
                    }
                }

                if (selectedDate != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (hasTime) String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute) else "No time set",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(onClick = { showTimePicker = true }) {
                            Text("Set Time")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTime = if (selectedDate != null) {
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = selectedDate!!
                            if (hasTime) {
                                set(Calendar.HOUR_OF_DAY, selectedHour)
                                set(Calendar.MINUTE, selectedMinute)
                            } else {
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                            }
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        calendar.timeInMillis
                    } else null
                    onConfirm(title, description, finalTime)
                },
                enabled = title.isNotBlank()
            ) {
                Text(if (reminder == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    hasTime = true
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            title = { Text("Select Time") },
            text = { TimePicker(state = timePickerState) }
        )
    }
}
