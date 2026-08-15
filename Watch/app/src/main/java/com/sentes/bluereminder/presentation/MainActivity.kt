package com.sentes.bluereminder.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.sentes.bluereminder.R
import com.sentes.bluereminder.data.Reminder
import com.sentes.bluereminder.presentation.theme.BlueReminderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainViewModel = viewModel()
            WearApp(viewModel)
        }
    }
}

@Composable
fun WearApp(viewModel: MainViewModel) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshReminders()
    }

    BlueReminderTheme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(
                scrollState = listState,
                edgeButton = {
                    EdgeButton(
                        onClick = { viewModel.refreshReminders() },
                        enabled = !isLoading,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                    ) {
                        Text("Refresh")
                    }
                },
            ) { contentPadding ->
                Box(modifier = Modifier.fillMaxSize()) {
                    TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
                        item {
                            ListHeader(
                                modifier =
                                    Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                                transformation = SurfaceTransformation(transformationSpec),
                            ) {
                                Text(text = stringResource(R.string.today_reminders))
                            }
                        }

                        if (!isLoading) {
                            if (reminders.isEmpty()) {
                                item {
                                    Text(
                                        text = "No reminders for today",
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            } else {
                                items(reminders, key = { it.id }) { reminder ->
                                    ReminderItem(reminder, transformationSpec)
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransformingLazyColumnItemScope.ReminderItem(
    reminder: Reminder,
    transformationSpec: TransformationSpec
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val isOverdue = reminder.reminderTime.isBefore(LocalDateTime.now())
    
    Button(
        onClick = { /* Handle click if needed */ },
        modifier = Modifier.fillMaxWidth()
            .transformedHeight(this, transformationSpec),
        transformation = SurfaceTransformation(transformationSpec),
        colors = if (isOverdue) {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        } else {
            ButtonDefaults.buttonColors()
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            Text(
                text = reminder.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = reminder.reminderTime.format(timeFormatter),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun DefaultPreview() {
    // Note: In a real preview you'd want a mock ViewModel or separate Composable for the content
    // WearApp(viewModel = ...) 
}
