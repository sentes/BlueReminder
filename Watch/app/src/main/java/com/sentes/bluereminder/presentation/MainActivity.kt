package com.sentes.bluereminder.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
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
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import androidx.wear.input.RemoteInputIntentHelper
import android.app.RemoteInput
import com.sentes.bluereminder.R
import com.sentes.bluereminder.data.Reminder
import com.sentes.bluereminder.presentation.theme.BlueReminderTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    val inputTextKey = "reminder_title"
    val remoteInputLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.let { intent ->
            val results: Bundle = RemoteInput.getResultsFromIntent(intent)
            val title: CharSequence? = results.getCharSequence(inputTextKey)
            if (title != null && title.isNotBlank()) {
                viewModel.addQuickReminder(title.toString())
            }
        }
    }

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
                        Text("Odśwież")
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
                                        text = "Brak przypomnień na dzisiaj",
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            } else {
                                items(reminders, key = { it.id }) { reminder ->
                                    ReminderItem(
                                        reminder,
                                        transformationSpec,
                                        onSnooze = { viewModel.snoozeReminder(reminder) },
                                        onDismiss = { viewModel.dismissReminder(reminder) }
                                    )
                                }
                            }
                        }

                        item {
                            val enterTitleLabel = stringResource(R.string.enter_reminder_title)
                            Button(
                                onClick = {
                                    val remoteInput = RemoteInput.Builder(inputTextKey)
                                        .setLabel(enterTitleLabel)
                                        .build()

                                    val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
                                    RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))

                                    remoteInputLauncher.launch(intent)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .transformedHeight(this, transformationSpec)
                                    .padding(bottom = 8.dp),
                                transformation = SurfaceTransformation(transformationSpec),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Text(stringResource(R.string.add_quick_reminder))
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
    transformationSpec: TransformationSpec,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val isOverdue = reminder.reminderTime.isBefore(LocalDateTime.now())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .transformedHeight(this, transformationSpec)
            .padding(vertical = 2.dp)
            .alpha(if (reminder.isCompleted) 0.75f else 1f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            transformation = SurfaceTransformation(transformationSpec),
            enabled = !reminder.isCompleted,
            colors = when {
                reminder.isCompleted -> ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface
                )
                isOverdue -> ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
                else -> ButtonDefaults.buttonColors()
            }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null
                )
                Text(
                    text = reminder.reminderTime.format(timeFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null
                )
            }
        }
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Button(
            onClick = onSnooze,
            modifier = Modifier.size(width = 52.dp, height = 52.dp),
            transformation = SurfaceTransformation(transformationSpec),
            enabled = !reminder.isCompleted,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        ) {
            Text("+1h", style = MaterialTheme.typography.labelSmall)
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
