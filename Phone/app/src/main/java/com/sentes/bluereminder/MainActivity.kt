package com.sentes.bluereminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sentes.bluereminder.data.Reminder
import com.sentes.bluereminder.service.MainForegroundService
import com.sentes.bluereminder.ui.ReminderViewModel
import com.sentes.bluereminder.ui.theme.BlueReminderTheme
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            MainForegroundService.startService(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlueReminderTheme {
                ReminderApp()
            }
        }

        checkAndStartForegroundService()
    }

    private fun checkAndStartForegroundService() {
        // Since minSdk is 36, POST_NOTIFICATIONS is required for foreground service visibility
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            MainForegroundService.startService(this)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

enum class ReminderGroup(val title: String) {
    Today("Dziś"),
    Tomorrow("Jutro"),
    Later("Później")
}

sealed class Screen {
    data object List : Screen()
    data class Editor(val reminder: Reminder? = null) : Screen()
}

private fun getReminderGroup(reminderTime: Long?): ReminderGroup {
    if (reminderTime == null) return ReminderGroup.Later
    
    val reminderDate = Instant.ofEpochMilli(reminderTime)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)

    return when {
        reminderDate.isBefore(tomorrow) -> ReminderGroup.Today
        reminderDate.isEqual(tomorrow) -> ReminderGroup.Tomorrow
        else -> ReminderGroup.Later
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ReminderApp(viewModel: ReminderViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }

    val reminders by viewModel.reminders.collectAsState()

    val groupedReminders = remember(reminders) {
        reminders.groupBy { getReminderGroup(it.reminderTime) }
            .toSortedMap(compareBy { it.ordinal })
    }

    when (val screen = currentScreen) {
        is Screen.List -> {
            ReminderListScreen(
                groupedReminders = groupedReminders,
                onAddReminder = { currentScreen = Screen.Editor() },
                onEditReminder = { currentScreen = Screen.Editor(it) },
                onToggleReminder = { viewModel.toggleReminderCompletion(it) },
                onDeleteReminder = { viewModel.deleteReminder(it) },
                onPostponeReminder = { viewModel.postponeReminder(it) }
            )
        }
        is Screen.Editor -> {
            BackHandler {
                currentScreen = Screen.List
            }
            ReminderEditorScreen(
                reminder = screen.reminder,
                onDismiss = { currentScreen = Screen.List },
                onConfirm = { title, desc, time ->
                    if (screen.reminder == null) {
                        viewModel.addReminder(title, desc, time)
                    } else {
                        viewModel.updateReminder(screen.reminder, title, desc, time)
                    }
                    currentScreen = Screen.List
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ReminderListScreen(
    groupedReminders: Map<ReminderGroup, List<Reminder>>,
    onAddReminder: () -> Unit,
    onEditReminder: (Reminder) -> Unit,
    onToggleReminder: (Reminder) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
    onPostponeReminder: (Reminder) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Powiadomienia") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddReminder) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj powiadomienie")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (groupedReminders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Brak powiadomień")
                }
            } else {
                LazyColumn {
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
                                onToggle = { onToggleReminder(reminder) },
                                onEdit = { onEditReminder(reminder) },
                                onDelete = { onDeleteReminder(reminder) },
                                onPostpone = { onPostponeReminder(reminder) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderItem(
    reminder: Reminder,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPostpone: () -> Unit
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
                        SimpleDateFormat("dd MMM, yyyy HH:mm", Locale.getDefault()).format(Date(time))
                    }
                    Text(
                        text = "Powiadomienie: $dateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onPostpone) {
                    Text("+1h")
                }
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
fun ReminderEditorScreen(
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (reminder == null) "Add Reminder" else "Edit Reminder") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val finalTime = if (selectedDate != null) {
                                val cal = Calendar.getInstance().apply {
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
                                cal.timeInMillis
                            } else null
                            onConfirm(title, description, finalTime)
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
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

            Text("Powiadomienie", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

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
                            Text("Date", style = MaterialTheme.typography.labelLarge)
                            Text(
                                text = selectedDate?.let {
                                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                                } ?: "No date set",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Button(onClick = { showDatePicker = true }) {
                            Text("Change")
                        }
                    }

                    if (selectedDate != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Time", style = MaterialTheme.typography.labelLarge)
                                Text(
                                    text = if (hasTime) String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute) else "No time set",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = {
                                    val cal = Calendar.getInstance().apply {
                                        timeInMillis = selectedDate!!
                                        set(Calendar.HOUR_OF_DAY, selectedHour)
                                        set(Calendar.MINUTE, selectedMinute)
                                        add(Calendar.HOUR_OF_DAY, 1)
                                    }
                                    selectedHour = cal.get(Calendar.HOUR_OF_DAY)
                                    selectedMinute = cal.get(Calendar.MINUTE)
                                    selectedDate = cal.apply {
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis
                                    hasTime = true
                                }) {
                                    Text("+1h")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = { showTimePicker = true }) {
                                    Text("Change")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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
