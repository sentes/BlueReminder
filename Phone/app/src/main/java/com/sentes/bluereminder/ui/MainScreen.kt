package com.sentes.bluereminder.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sentes.bluereminder.MainTab
import com.sentes.bluereminder.ReminderGroup
import com.sentes.bluereminder.data.Reminder
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    reminders: List<Reminder>,
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onAddReminder: () -> Unit,
    onEditReminder: (Reminder) -> Unit,
    onToggleReminder: (Reminder) -> Unit,
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
                        onPostponeReminder = onPostponeReminder
                    )
                }
            }
        }
    }
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
