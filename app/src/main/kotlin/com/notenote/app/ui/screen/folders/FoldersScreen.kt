package com.notenote.app.ui.screen.folders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notenote.app.NotenoteApplication
import com.notenote.app.domain.model.Folder
import com.notenote.app.ui.common.VmFactory
import com.notenote.app.ui.components.ConfirmDialog
import com.notenote.app.ui.components.EmojiPickerDialog
import com.notenote.app.ui.components.EmptyState
import com.notenote.app.ui.components.MoveToFolderDialog
import com.notenote.app.ui.components.NoteColorDialog
import com.notenote.app.ui.components.TextInputDialog
import com.notenote.app.ui.theme.paperTexture

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(
    onOpenFolder: (Long, String) -> Unit,
) {
    val app = LocalContext.current.applicationContext as NotenoteApplication
    val viewModel: FoldersViewModel = viewModel(
        factory = VmFactory { FoldersViewModel(app.container) },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val darkTheme = isSystemInDarkTheme()

    var createDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<FolderWithCount?>(null) }
    var deleteTarget by remember { mutableStateOf<FolderWithCount?>(null) }
    var moveNotesChoice by remember { mutableStateOf<FolderWithCount?>(null) }

    Scaffold(
        modifier = Modifier.paperTexture(enabled = true, darkTheme = darkTheme),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Folders") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { createDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "New folder")
            }
        },
    ) { padding ->
        if (state.folders.isEmpty()) {
            Column(Modifier.padding(padding)) {
                EmptyState(
                    icon = "🗂️",
                    title = "No folders yet",
                    subtitle = "Create folders to organize appliances, vehicles, measurements and more.",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp,
                    bottom = 90.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.folders, key = { it.folder.id }) { entry ->
                    val folder = entry.folder
                    val container = com.notenote.app.ui.theme.noteContainerColor(folder.colorKey, darkTheme)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenFolder(folder.id, folder.name) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = container),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp),
                        ) {
                            Text(
                                folder.icon ?: "📁",
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            Column(
                                Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp),
                            ) {
                                Text(folder.name, style = MaterialTheme.typography.titleMedium)
                                val counts = buildList {
                                    add("${entry.noteCount} note" + if (entry.noteCount == 1) "" else "s")
                                    if (entry.childCount > 0) add("${entry.childCount} subfolder" + if (entry.childCount == 1) "" else "s")
                                }
                                Text(
                                    counts.joinToString(" · "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                "Edit",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable { editTarget = entry }
                                    .padding(8.dp),
                            )
                            Text(
                                "Delete",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .clickable { deleteTarget = entry }
                                    .padding(8.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    if (createDialog) {
        FolderEditDialog(
            title = "New folder",
            initialName = "",
            initialIcon = null,
            initialColorKey = null,
            onDismiss = { createDialog = false },
            onSave = { name, icon, colorKey ->
                createDialog = false
                viewModel.saveFolder(name, icon, colorKey, parentId = null)
            },
        )
    }

    editTarget?.let { entry ->
        FolderEditDialog(
            title = "Edit folder",
            initialName = entry.folder.name,
            initialIcon = entry.folder.icon,
            initialColorKey = entry.folder.colorKey,
            onDismiss = { editTarget = null },
            onSave = { name, icon, colorKey ->
                editTarget = null
                viewModel.saveFolder(name, icon, colorKey, parentId = entry.folder.parentId, folderId = entry.folder.id)
            },
        )
    }

    deleteTarget?.let { entry ->
        ConfirmDialog(
            title = "Delete folder \"${entry.folder.name}\"?",
            message = "Its ${entry.noteCount} note" + if (entry.noteCount == 1) "" else "s" +
                " will be kept and moved back to General notes.",
            onConfirm = {
                deleteTarget = null
                viewModel.deleteFolder(entry.folder.id, FolderDeletionChoice.DetachNotes)
            },
            onDismiss = { deleteTarget = null },
        )
    }

    moveNotesChoice?.let { entry ->
        MoveToFolderDialog(
            title = "Move notes to…",
            folders = state.folders.filter { it.folder.id != entry.folder.id }.map { it.folder },
            currentFolderId = null,
            onDismiss = { moveNotesChoice = null },
            onMove = { target ->
                moveNotesChoice = null
                viewModel.deleteFolder(entry.folder.id, FolderDeletionChoice.MoveToFolder(target))
            },
        )
    }
}

@Composable
private fun FolderEditDialog(
    title: String,
    initialName: String,
    initialIcon: String?,
    initialColorKey: String?,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String?, colorKey: String?) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var icon by remember { mutableStateOf(initialIcon) }
    var colorKey by remember { mutableStateOf(initialColorKey) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    val darkTheme = isSystemInDarkTheme()

    if (showEmojiPicker) {
        EmojiPickerDialog(
            title = "Folder icon",
            onDismiss = { showEmojiPicker = false },
            onPicked = { emoji ->
                showEmojiPicker = false
                icon = emoji
            },
        )
    }
    if (showColorPicker) {
        NoteColorDialog(
            currentKey = colorKey,
            darkTheme = darkTheme,
            onDismiss = { showColorPicker = false },
            onPicked = { key ->
                showColorPicker = false
                colorKey = key
            },
        )
    }

    com.notenote.app.ui.components.NotenoteAlertDialog(
        title = title,
        confirmLabel = "Save",
        onConfirm = { onSave(name, icon, colorKey) },
        onDismiss = onDismiss,
    ) {
        androidx.compose.material3.OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Folder name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Text(
                icon ?: "＋ Icon",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { showEmojiPicker = true },
            )
            val swatch = com.notenote.app.ui.theme.NoteColorPalette.firstOrNull { it.key == colorKey }
            val swatchColor = swatch?.let { if (darkTheme) it.dark else it.light }
                ?: androidx.compose.ui.graphics.Color.Transparent
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(swatchColor)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable { showColorPicker = true },
            )
            Text(
                "＋ Color",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { showColorPicker = true },
            )
        }
    }
}
