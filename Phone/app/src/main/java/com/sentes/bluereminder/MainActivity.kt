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
import com.sentes.bluereminder.ui.MainScreen
import com.sentes.bluereminder.ui.ReminderEditorScreen
import com.sentes.bluereminder.ui.ReminderItem
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
                },
                onDelete = {
                    screen.reminder?.let { viewModel.deleteReminder(it) }
                    currentScreen = Screen.List
                }
            )
        }
    }
}