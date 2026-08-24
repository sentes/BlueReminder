package com.sentes.bluereminder.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


@Composable
fun SnoozeDropdown(onSnoozeSelected: (Long) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Snooze, contentDescription = "Drzemka")
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
