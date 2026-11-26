package com.notenote.app.ui.screen.reminders

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notenote.app.NotenoteApplication
import com.notenote.app.notification.Notifications
import com.notenote.app.ui.common.VmFactory
import com.notenote.app.ui.components.ConfirmDialog
import com.notenote.app.ui.components.EmptyState
import com.notenote.app.ui.theme.paperTexture
import com.notenote.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    onOpenNote: (Long) -> Unit,
) {
    val app = LocalContext.current.applicationContext as NotenoteApplication
    val viewModel: RemindersViewModel = viewModel(
        factory = VmFactory { RemindersViewModel(app.container) },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val darkTheme = isSystemInDarkTheme()
    val context = LocalContext.current

    var removeTarget by remember { mutableStateOf<ReminderEntry?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    LaunchedEffect(state.entries.size) {
        if (state.entries.isNotEmpty() &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !Notifications.canPostNotifications(context)
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        modifier = Modifier.paperTexture(enabled = true, darkTheme = darkTheme),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Reminders") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        if (state.entries.isEmpty()) {
            Column(Modifier.padding(padding)) {
                EmptyState(
                    icon = "⏰",
                    title = "No reminders",
                    subtitle = "Open a note, add a reminder and Notenote will notify you in time.",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.entries, key = { it.note.id }) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenNote(entry.note.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (entry.overdue) {
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp),
                        ) {
                            Text(
                                entry.note.icon ?: "⏰",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(end = 12.dp),
                            )
                            Column(Modifier.weight(1f)) {
                                Text(
                                    entry.note.displayTitle.ifBlank { "(Untitled note)" },
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                )
                                Text(
                                    DateUtils.reminderLabel(entry.note.reminderAt ?: 0L),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (entry.overdue) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                            TextButton(onClick = { removeTarget = entry }) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }
        }
    }

    if (removeTarget != null) {
        ConfirmDialog(
            title = "Remove reminder?",
            message = "The note itself stays; only the reminder is removed.",
            confirmLabel = "Remove",
            onConfirm = {
                removeTarget?.let { viewModel.removeReminder(it.note.id) }
                removeTarget = null
            },
            onDismiss = { removeTarget = null },
        )
    }
}
