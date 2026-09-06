package com.sentes.bluereminder.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.sentes.bluereminder.data.RecurrenceType
import com.sentes.bluereminder.data.Reminder
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderEditorScreen(
    reminder: Reminder? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long?, Long?, String) -> Unit,
    onDelete: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val titleFocusRequester = remember { FocusRequester() }

    LaunchedEffect(reminder) {
        if (reminder == null) {
            titleFocusRequester.requestFocus()
        }
    }

    // Event Time States
    val eventCalendar = remember(reminder) {
        Calendar.getInstance().apply {
            if (reminder?.eventTime != null) {
                timeInMillis = reminder.eventTime
            } else {
                add(Calendar.HOUR_OF_DAY, 1)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }
    }
    val initialEventDate = remember(reminder) {
        reminder?.eventTime?.let {
            Calendar.getInstance().apply {
                timeInMillis = it
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
    }
    var eventDate by remember { mutableStateOf<Long?>(initialEventDate) }
    var eventHour by remember { mutableIntStateOf(eventCalendar.get(Calendar.HOUR_OF_DAY)) }
    var eventMinute by remember { mutableIntStateOf(eventCalendar.get(Calendar.MINUTE)) }
    var hasEventTime by remember { mutableStateOf(reminder?.eventTime != null) }

    // Reminder Time States
    val reminderCalendar = remember(reminder) {
        Calendar.getInstance().apply {
            if (reminder?.reminderTime != null) {
                timeInMillis = reminder.reminderTime
            } else {
                add(Calendar.HOUR_OF_DAY, 1)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }
    }
    val initialReminderDate = remember(reminder) {
        reminder?.reminderTime?.let {
            Calendar.getInstance().apply {
                timeInMillis = it
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
    }
    var reminderDate by remember { mutableStateOf<Long?>(initialReminderDate) }
    var reminderHour by remember { mutableIntStateOf(reminderCalendar.get(Calendar.HOUR_OF_DAY)) }
    var reminderMinute by remember { mutableIntStateOf(reminderCalendar.get(Calendar.MINUTE)) }
    var hasReminderTime by remember { mutableStateOf(reminder?.reminderTime != null || reminder == null) }

    var title by remember { mutableStateOf(reminder?.title ?: "") }
    var description by remember { mutableStateOf(reminder?.description ?: "") }
    var recurrenceType by remember { mutableStateOf(reminder?.recurrenceType ?: "None") }
    var recurrenceMenuExpanded by remember { mutableStateOf(false) }

    var showEventDatePicker by remember { mutableStateOf(false) }
    var showEventTimePicker by remember { mutableStateOf(false) }
    var showReminderDatePicker by remember { mutableStateOf(false) }
    var showReminderTimePicker by remember { mutableStateOf(false) }

    val eventDatePickerState = rememberDatePickerState(initialSelectedDateMillis = initialEventDate)
    val eventTimePickerState = rememberTimePickerState(initialHour = eventHour, initialMinute = eventMinute)
    val reminderDatePickerState = rememberDatePickerState(initialSelectedDateMillis = initialReminderDate)
    val reminderTimePickerState = rememberTimePickerState(initialHour = reminderHour, initialMinute = reminderMinute)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (reminder == null) "Dodaj powiadomienie" else "Edytuj powiadomienie") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                },
                actions = {
                    if (reminder != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentPadding = PaddingValues(16.dp, 8.dp)
            ) {
                Button(
                    onClick = {
                        val finalReminderTime = if (reminderDate != null) {
                            Calendar.getInstance().apply {
                                timeInMillis = reminderDate!!
                                if (hasReminderTime) {
                                    set(Calendar.HOUR_OF_DAY, reminderHour)
                                    set(Calendar.MINUTE, reminderMinute)
                                } else {
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                }
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                        } else null

                        val finalEventTime = if (eventDate != null) {
                            Calendar.getInstance().apply {
                                timeInMillis = eventDate!!
                                if (hasEventTime) {
                                    set(Calendar.HOUR_OF_DAY, eventHour)
                                    set(Calendar.MINUTE, eventMinute)
                                } else {
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                }
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                        } else null

                        onConfirm(title, description, finalReminderTime, finalEventTime, recurrenceType)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Zapisz")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tytuł") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester)
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Opis") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Reminder Time Section
            Text("Powiadomienie", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            DateTimeCard(
                date = reminderDate,
                hour = reminderHour,
                minute = reminderMinute,
                hasTime = hasReminderTime,
                onDateClick = {
                    focusManager.clearFocus()
                    showReminderDatePicker = true
                },
                onTimeClick = {
                    focusManager.clearFocus()
                    showReminderTimePicker = true
                },
                onClearClick = {
                    focusManager.clearFocus()
                    reminderDate = null
                    hasReminderTime = false
                },
                onSnoozeClick = { duration ->
                    focusManager.clearFocus()
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = reminderDate ?: System.currentTimeMillis()
                        set(Calendar.HOUR_OF_DAY, reminderHour)
                        set(Calendar.MINUTE, reminderMinute)
                        add(Calendar.MILLISECOND, duration.toInt())
                    }
                    reminderHour = cal.get(Calendar.HOUR_OF_DAY)
                    reminderMinute = cal.get(Calendar.MINUTE)
                    reminderDate = cal.apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    hasReminderTime = true
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Powtarzanie", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        recurrenceMenuExpanded = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(RecurrenceType.entries.find { it.name == recurrenceType }?.title ?: "Brak")
                }
                DropdownMenu(
                    expanded = recurrenceMenuExpanded,
                    onDismissRequest = { recurrenceMenuExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    RecurrenceType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.title) },
                            onClick = {
                                recurrenceType = type.name
                                recurrenceMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Event Time Section
            Text("Termin wydarzenia", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            DateTimeCard(
                date = eventDate,
                hour = eventHour,
                minute = eventMinute,
                hasTime = hasEventTime,
                onDateClick = {
                    focusManager.clearFocus()
                    showEventDatePicker = true
                },
                onTimeClick = {
                    focusManager.clearFocus()
                    showEventTimePicker = true
                },
                onClearClick = {
                    focusManager.clearFocus()
                    eventDate = null
                    hasEventTime = false
                },
                onSnoozeClick = { duration ->
                    focusManager.clearFocus()
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = eventDate ?: System.currentTimeMillis()
                        set(Calendar.HOUR_OF_DAY, eventHour)
                        set(Calendar.MINUTE, eventMinute)
                        add(Calendar.MILLISECOND, duration.toInt())
                    }
                    eventHour = cal.get(Calendar.HOUR_OF_DAY)
                    eventMinute = cal.get(Calendar.MINUTE)
                    eventDate = cal.apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    hasEventTime = true
                }
            )
        }
    }

    // Date/Time Picker Dialogs
    if (showEventDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEventDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val newDate = eventDatePickerState.selectedDateMillis
                    eventDate = newDate
                    
                    newDate?.let {
                        val cal = Calendar.getInstance().apply { timeInMillis = it }
                        val today = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DATE, 1) }

                        if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                            val now = Calendar.getInstance()
                            now.add(Calendar.HOUR_OF_DAY, 1)
                            eventHour = now.get(Calendar.HOUR_OF_DAY)
                            eventMinute = 0
                        } else if (cal.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
                            cal.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR)) {
                            eventHour = 8
                            eventMinute = 0
                        }
                    }
                    showEventDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEventDatePicker = false }) { Text("Anuluj") }
            }
        ) {
            DatePicker(state = eventDatePickerState)
        }
    }

    if (showEventTimePicker) {
        AlertDialog(
            onDismissRequest = { showEventTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    eventHour = eventTimePickerState.hour
                    eventMinute = eventTimePickerState.minute
                    hasEventTime = true
                    showEventTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEventTimePicker = false }) { Text("Anuluj") }
            },
            title = { Text("Wybierz godzinę") },
            text = { TimePicker(state = eventTimePickerState) }
        )
    }

    if (showReminderDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showReminderDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val newDate = reminderDatePickerState.selectedDateMillis
                    reminderDate = newDate

                    newDate?.let {
                        val cal = Calendar.getInstance().apply { timeInMillis = it }
                        val today = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DATE, 1) }

                        if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                            val now = Calendar.getInstance()
                            now.add(Calendar.HOUR_OF_DAY, 1)
                            reminderHour = now.get(Calendar.HOUR_OF_DAY)
                            reminderMinute = 0
                        } else if (cal.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
                            cal.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR)) {
                            reminderHour = 8
                            reminderMinute = 0
                        }
                    }
                    showReminderDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showReminderDatePicker = false }) { Text("Anuluj") }
            }
        ) {
            DatePicker(state = reminderDatePickerState)
        }
    }

    if (showReminderTimePicker) {
        AlertDialog(
            onDismissRequest = { showReminderTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    reminderHour = reminderTimePickerState.hour
                    reminderMinute = reminderTimePickerState.minute
                    hasReminderTime = true
                    showReminderTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showReminderTimePicker = false }) { Text("Anuluj") }
            },
            title = { Text("Wybierz godzinę") },
            text = { TimePicker(state = reminderTimePickerState) }
        )
    }
}
