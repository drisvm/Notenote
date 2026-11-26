package com.notenote.app.ui.screen.editor

import android.content.Intent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notenote.app.NotenoteApplication
import com.notenote.app.domain.model.Note
import com.notenote.app.ui.common.VmFactory
import com.notenote.app.ui.components.ConfirmDialog
import com.notenote.app.ui.components.EmojiPickerDialog
import com.notenote.app.ui.components.MoveToFolderDialog
import com.notenote.app.ui.components.NoteColorDialog
import com.notenote.app.ui.components.PaperChip
import com.notenote.app.ui.components.TextInputDialog
import com.notenote.app.ui.theme.paperTexture
import com.notenote.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEditorScreen(
    noteId: Long,
    presetFolderId: Long?,
    onBack: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as NotenoteApplication
    val viewModel: NoteEditorViewModel = viewModel(
        key = "editor_$noteId",
        factory = VmFactory { NoteEditorViewModel(app.container, noteId, presetFolderId) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val darkTheme = isSystemInDarkTheme()

    var showIconPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showMoveToFolder by remember { mutableStateOf(false) }
    var showReminderPicker by remember { mutableStateOf(false) }
    var showAddTag by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showOverflow by remember { mutableStateOf(false) }

    val note = state.note
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose { viewModel.persistNow() }
    }

    val plainFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
    )

    Scaffold(
        modifier = Modifier.paperTexture(enabled = true, darkTheme = darkTheme),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.persistNow()
                        onBack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = when {
                            state.savedOnce && note != null && note.id != 0L -> "Saved"
                            state.isNew -> "New note"
                            else -> "Edit note"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                actions = {
                    if (note != null) {
                        IconButton(onClick = { viewModel.update { it.copy(isFavorite = !it.isFavorite) } }) {
                            Icon(
                                imageVector = if (note.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (note.isFavorite) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                        IconButton(onClick = { viewModel.update { it.copy(isPinned = !it.isPinned) } }) {
                            Icon(
                                imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Pin",
                                tint = if (note.isPinned) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                        Box {
                            IconButton(onClick = { showOverflow = true }) {
                                Icon(Icons.Filled.MoreHoriz, contentDescription = "More")
                            }
                            if (showOverflow) {
                                EditorOverflowMenu(
                                    onDismiss = { showOverflow = false },
                                    onShare = {
                                        showOverflow = false
                                        note?.let { shareNote(context, it) }
                                    },
                                    onDelete = {
                                        showOverflow = false
                                        showDeleteConfirm = true
                                    },
                                    onDuplicate = {
                                        showOverflow = false
                                        viewModel.duplicateCurrent { onBack() }
                                    },
                                    onChangeColor = {
                                        showOverflow = false
                                        showColorPicker = true
                                    },
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        if (note == null) {
            Box(Modifier.fillMaxSize())
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            if (!note.icon.isNullOrBlank()) {
                Text(
                    note.icon,
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                )
            }
            Row {
                Text(
                    if (note.icon.isNullOrBlank()) "+ Icon" else "Change icon",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { showIconPicker = true }
                        .padding(end = 20.dp, bottom = 6.dp),
                )
                Text(
                    note.reminderAt?.let { "\u23F0 ${DateUtils.reminderLabel(it)}" } ?: "+ Reminder",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (note.reminderAt != null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .clickable { showReminderPicker = true }
                        .padding(end = 20.dp, bottom = 6.dp),
                )
                Text(
                    "+ Tag",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { showAddTag = true },
                )
            }
            OutlinedTextField(
                value = note.title,
                onValueChange = { value -> viewModel.update { it.copy(title = value) } },
                placeholder = { Text("Title") },
                textStyle = MaterialTheme.typography.headlineSmall,
                singleLine = true,
                shape = RectangleShape,
                colors = plainFieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
            )
            OutlinedTextField(
                value = note.content,
                onValueChange = { value -> viewModel.update { it.copy(content = value) } },
                placeholder = { Text("Write here\u2026") },
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = RectangleShape,
                colors = plainFieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 240.dp),
            )
            if (note.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp),
                ) {
                    note.tags.forEach { tag ->
                        PaperChip(
                            text = "\u00D7 $tag",
                            modifier = Modifier.clickable {
                                viewModel.update { it.copy(tags = it.tags - tag) }
                            },
                        )
                    }
                }
            }
            val folderName = state.folders.firstOrNull { it.id == note.folderId }?.name
            Text(
                text = folderName
                    ?.let { "Folder: $it \u2014 tap to change" }
                    ?: "No folder \u2014 tap to assign",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { showMoveToFolder = true }
                    .padding(vertical = 12.dp),
            )
            Text(
                "Created ${DateUtils.formatDateTime(note.createdAt)} \u00B7 " +
                    "Modified ${DateUtils.formatDateTime(note.modifiedAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }

    if (showIconPicker) {
        EmojiPickerDialog(
            title = "Note icon",
            onDismiss = { showIconPicker = false },
            onPicked = { emoji ->
                showIconPicker = false
                viewModel.update { it.copy(icon = emoji) }
            },
        )
    }

    if (showColorPicker && note != null) {
        NoteColorDialog(
            currentKey = note.colorKey,
            darkTheme = darkTheme,
            onDismiss = { showColorPicker = false },
            onPicked = { key ->
                showColorPicker = false
                viewModel.update { it.copy(colorKey = key) }
            },
        )
    }

    if (showMoveToFolder && note != null) {
        MoveToFolderDialog(
            title = "Move to folder",
            folders = state.folders,
            currentFolderId = note.folderId,
            onDismiss = { showMoveToFolder = false },
            onMove = { target ->
                showMoveToFolder = false
                viewModel.update { it.copy(folderId = target) }
            },
        )
    }

    if (showReminderPicker && note != null) {
        ReminderPickerDialog(
            initial = note.reminderAt,
            onDismiss = { showReminderPicker = false },
            onConfirm = { timestamp ->
                showReminderPicker = false
                viewModel.update { it.copy(reminderAt = timestamp) }
            },
            onRemove = {
                showReminderPicker = false
                viewModel.update { it.copy(reminderAt = null) }
            },
        )
    }

    if (showAddTag) {
        TextInputDialog(
            title = "Add tag",
            initialText = "",
            placeholder = "e.g. Warranty",
            onConfirm = { value ->
                showAddTag = false
                if (value.isNotBlank()) {
                    viewModel.update { it.copy(tags = (it.tags + value).distinct()) }
                }
            },
            onDismiss = { showAddTag = false },
        )
    }

    if (showDeleteConfirm && note != null && note.id != 0L) {
        ConfirmDialog(
            title = "Delete note?",
            message = "The note is moved to the Archive.",
            onConfirm = {
                showDeleteConfirm = false
                viewModel.archiveCurrent()
                onBack()
            },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}

@Composable
private fun EditorOverflowMenu(
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onChangeColor: () -> Unit,
) {
    androidx.compose.material3.DropdownMenu(expanded = true, onDismissRequest = onDismiss) {
        androidx.compose.material3.DropdownMenuItem(text = { Text("Share") }, onClick = onShare)
        androidx.compose.material3.DropdownMenuItem(text = { Text("Duplicate") }, onClick = onDuplicate)
        androidx.compose.material3.DropdownMenuItem(text = { Text("Change color") }, onClick = onChangeColor)
        androidx.compose.material3.DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) }, onClick = onDelete)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderPickerDialog(
    initial: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    onRemove: () -> Unit,
) {
    var step by remember { mutableStateOf(0) }
    var dateMillis by remember {
        mutableStateOf(initial ?: System.currentTimeMillis())
    }
    var hour by remember { mutableStateOf(9) }
    var minute by remember { mutableStateOf(0) }

    if (step == 0) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = dateMillis,
        )
        NotenoteAlertDialogShim(
            title = "Reminder date",
            confirmLabel = "Next",
            onConfirm = {
                dateState.selectedDateMillis?.let { dateMillis = it }
                // Keep existing time of day if editing, otherwise default 09:00.
                if (initial != null) {
                    val (baseDate, time) = DateUtils.splitDateTime(initial)
                    if (dateState.selectedDateMillis == baseDate) {
                        hour = time.first
                        minute = time.second
                    }
                }
                step = 1
            },
            onDismiss = onDismiss,
            dismissLabel = "Cancel",
        ) {
            DatePicker(state = dateState)
        }
    } else {
        val timeState = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
        NotenoteAlertDialogShim(
            title = "Reminder time",
            confirmLabel = "Set reminder",
            onConfirm = {
                val timestamp = DateUtils.combineDateAndTime(dateMillis, timeState.hour, timeState.minute)
                onConfirm(timestamp)
            },
            onDismiss = onDismiss,
            dismissLabel = "Back",
            onDismissRequested = { step = 0 },
        ) {
            TimePicker(state = timeState)
        }
    }
}

@Composable
private fun NotenoteAlertDialogShim(
    title: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String,
    onDismissRequested: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = {
            if (onDismissRequested != null) onDismissRequested() else onDismiss()
        },
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(title) },
        text = { content() },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissLabel) }
        },
    )
}

private fun shareNote(context: android.content.Context, note: Note) {
    val text = buildString {
        if (note.title.isNotBlank()) appendLine(note.title)
        if (note.content.isNotBlank()) {
            if (isNotEmpty()) appendLine()
            append(note.content)
        }
        if (note.tags.isNotEmpty()) {
            appendLine()
            append(note.tags.joinToString(", ", prefix = "Tags: "))
        }
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share note"))
}
