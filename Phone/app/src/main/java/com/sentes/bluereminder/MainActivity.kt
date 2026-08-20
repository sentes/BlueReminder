package com.sentes.bluereminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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

enum class MainTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Reminders("Powiadomienia", Icons.Default.Notifications),
    Calendar("Kalendarz", Icons.Default.Edit)
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
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }
    var selectedTab by remember { mutableStateOf(MainTab.Reminders) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(viewModel.getRemindersJson().toByteArray())
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                val jsonString = inputStream.bufferedReader().use { reader -> reader.readText() }
                viewModel.importRemindersFromJson(jsonString)
            }
        }
    }

    val reminders by viewModel.reminders.collectAsState()

    when (val screen = currentScreen) {
        is Screen.List -> {
            MainScreen(
                reminders = reminders,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onAddReminder = { currentScreen = Screen.Editor() },
                onEditReminder = { currentScreen = Screen.Editor(it) },
                onToggleReminder = { viewModel.toggleReminderCompletion(it) },
                onDeleteReminder = { viewModel.deleteReminder(it) },
                onPostponeReminder = { reminder, duration -> viewModel.postponeReminder(reminder, duration) },
                onExportJson = { exportLauncher.launch("reminders_backup.json") },
                onImportJson = { importLauncher.launch(arrayOf("application/json")) }
            )
        }
        is Screen.Editor -> {
            BackHandler {
                currentScreen = Screen.List
            }
            ReminderEditorScreen(
                reminder = screen.reminder,
                onDismiss = { currentScreen = Screen.List },
                onConfirm = { title, desc, reminderTime, eventTime ->
                    if (screen.reminder == null) {
                        viewModel.addReminder(title, desc, reminderTime, eventTime)
                    } else {
                        viewModel.updateReminder(screen.reminder, title, desc, reminderTime, eventTime)
                    }
                    currentScreen = Screen.List
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    reminders: List<Reminder>,
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onAddReminder: () -> Unit,
    onEditReminder: (Reminder) -> Unit,
    onToggleReminder: (Reminder) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
    onPostponeReminder: (Reminder, Long) -> Unit,
    onExportJson: () -> Unit,
    onImportJson: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(selectedTab.title) },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Eksportuj do JSON") },
                            onClick = {
                                showMenu = false
                                onExportJson()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Importuj z JSON") },
                            onClick = {
                                showMenu = false
                                onImportJson()
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { onTabSelected(tab) },
                        label = { Text(tab.title) },
                        icon = { Icon(tab.icon, contentDescription = tab.title) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddReminder) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj powiadomienie")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                MainTab.Reminders -> {
                    val groupedReminders = remember(reminders) {
                        reminders.groupBy { getReminderGroup(it.reminderTime) }
                            .toSortedMap(compareBy { it.ordinal })
                    }
                    ReminderListContent(
                        groupedReminders = groupedReminders,
                        onToggleReminder = onToggleReminder,
                        onEditReminder = onEditReminder,
                        onDeleteReminder = onDeleteReminder,
                        onPostponeReminder = onPostponeReminder
                    )
                }
                MainTab.Calendar -> {
                    val calendarReminders = remember(reminders) {
                        reminders.filter { it.eventTime != null }
                            .sortedBy { it.eventTime }
                    }
                    ReminderCalendarContent(
                        reminders = calendarReminders,
                        onToggleReminder = onToggleReminder,
                        onEditReminder = onEditReminder,
                        onDeleteReminder = onDeleteReminder,
                        onPostponeReminder = onPostponeReminder
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ReminderListContent(
    groupedReminders: Map<ReminderGroup, List<Reminder>>,
    onToggleReminder: (Reminder) -> Unit,
    onEditReminder: (Reminder) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
    onPostponeReminder: (Reminder, Long) -> Unit
) {
    if (groupedReminders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Brak powiadomień")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                        onPostpone = { duration -> onPostponeReminder(reminder, duration) }
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ReminderCalendarContent(
    reminders: List<Reminder>,
    onToggleReminder: (Reminder) -> Unit,
    onEditReminder: (Reminder) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
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

        LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                        onDelete = { onDeleteReminder(reminder) },
                        onPostpone = { duration -> onPostponeReminder(reminder, duration) }
                    )
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
                            SimpleDateFormat("dd MMM, yyyy HH:mm", Locale.getDefault()).format(Date(time))
                        }
                        Text(
                            text = "Termin: $dateStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    reminder.reminderTime?.let { time ->
                        val dateStr = remember(time) {
                            SimpleDateFormat("dd MMM, yyyy HH:mm", Locale.getDefault()).format(Date(time))
                        }
                        Text(
                            text = "Powiadomienie: $dateStr",
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

@Composable
fun SnoozeDropdown(onSnoozeSelected: (Long) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        TextButton(onClick = { expanded = true }) {
            Text("+...")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("+1h") },
                onClick = {
                    onSnoozeSelected(60 * 60 * 1000L)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("+2h") },
                onClick = {
                    onSnoozeSelected(2 * 60 * 60 * 1000L)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("+1d") },
                onClick = {
                    onSnoozeSelected(24 * 60 * 60 * 1000L)
                    expanded = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderEditorScreen(
    reminder: Reminder? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long?, Long?) -> Unit
) {
    val focusManager = LocalFocusManager.current
    // Event Time States
    val eventCalendar = remember(reminder) {
        Calendar.getInstance().apply {
            if (reminder?.eventTime != null) {
                timeInMillis = reminder.eventTime
            } else {
                add(Calendar.HOUR_OF_DAY, 1)
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

                        onConfirm(title, description, finalReminderTime, finalEventTime)
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
        }
    }

    // Date/Time Picker Dialogs
    if (showEventDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEventDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    eventDate = eventDatePickerState.selectedDateMillis
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
                    reminderDate = reminderDatePickerState.selectedDateMillis
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
                        text = date?.let {
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                        } ?: "Nie ustawiono",
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
