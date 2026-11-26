package com.notenote.app.ui.screen.list

import android.content.Intent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notenote.app.NotenoteApplication
import com.notenote.app.domain.model.Folder
import com.notenote.app.domain.model.ListMode
import com.notenote.app.domain.model.Note
import com.notenote.app.domain.model.SortOption
import com.notenote.app.ui.common.VmFactory
import com.notenote.app.ui.components.ConfirmDialog
import com.notenote.app.ui.components.EmptyState
import com.notenote.app.ui.components.MoveToFolderDialog
import com.notenote.app.ui.components.NoteColorDialog
import com.notenote.app.ui.components.NoteAction
import com.notenote.app.ui.components.NoteActionsDialog
import com.notenote.app.ui.components.NoteCard
import com.notenote.app.ui.components.SortMenu
import com.notenote.app.ui.theme.noteContainerColor
import com.notenote.app.ui.theme.paperTexture

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    mode: ListMode,
    folderId: Long?,
    folderTitle: String,
    onOpenNote: (Long) -> Unit,
    onCreateNote: (Long?) -> Unit,
    onOpenArchive: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as NotenoteApplication
    val context = LocalContext.current
    val viewModel: NoteListViewModel = viewModel(
        key = "list_${mode.name}_$folderId",
        factory = VmFactory { NoteListViewModel(app.container, mode, folderId) },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val darkTheme = isSystemInDarkTheme()

    var actionsTarget by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showActions by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showMoveToFolder by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val folderNames: Map<Long, String> = state.folders.associate { it.id to it.name }

    val resolvedTitle = if (folderId != null) {
        state.folders.firstOrNull { it.id == folderId }?.name ?: folderTitle
    } else {
        folderTitle
    }

    val heading = when {
        mode == ListMode.ARCHIVE -> "Archive"
        state.searchActive -> "Search"
        else -> resolvedTitle
    }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        modifier = Modifier.paperTexture(enabled = true, darkTheme = darkTheme),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (state.selectionMode) {
                SelectionTopBar(
                    count = state.selectedIds.size,
                    onClose = viewModel::clearSelection,
                    onSelectAll = viewModel::selectAll,
                    onMore = {
                        actionsTarget = state.selectedIds
                        showActions = true
                    },
                )
            } else {
                ListTopBar(
                    title = heading,
                    searchActive = state.searchActive,
                    query = state.query,
                    onQueryChange = viewModel::setQuery,
                    onSearchToggle = { viewModel.setSearchActive(!state.searchActive) },
                    showArchiveEntry = mode == ListMode.ALL,
                    onOpenArchive = onOpenArchive,
                    currentSort = state.sortOption,
                    ascending = state.sortAscending,
                    onOptionSelected = { viewModel.updateSorting(it, state.sortAscending) },
                    onToggleDirection = viewModel::toggleSortDirection,
                )
            }
        },
        floatingActionButton = {
            if (!state.selectionMode && mode != ListMode.ARCHIVE) {
                ExtendedFloatingActionButton(
                    onClick = { onCreateNote(folderId) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("New note") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            if (state.searchActive) {
                Text(
                    "${state.notes.size} result" + if (state.notes.size == 1) "" else "s",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 6.dp),
                )
            }
            if (state.notes.isEmpty()) {
                EmptyState(
                    icon = if (state.searchActive) "🔍" else "🗒️",
                    title = if (state.searchActive) "Nothing found" else emptyTitle(mode),
                    subtitle = if (state.searchActive) "Try a different search term." else emptySubtitle(mode),
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            folderName = note.folderId?.let { folderNames[it] },
                            timeText = com.notenote.app.util.DateUtils.formatRelative(note.modifiedAt),
                            selected = note.id in state.selectedIds,
                            darkTheme = darkTheme,
                            onClick = {
                                if (state.selectionMode) viewModel.toggleSelected(note.id) else onOpenNote(note.id)
                            },
                            onLongClick = {
                                if (state.selectionMode) {
                                    viewModel.toggleSelected(note.id)
                                } else {
                                    viewModel.enterSelection(note.id)
                                    actionsTarget = setOf(note.id)
                                    showActions = true
                                }
                            },
                        )
                    }
                    item { Box(Modifier.padding(bottom = 70.dp)) }
                }
            }
        }
    }

    if (showActions && actionsTarget.isNotEmpty()) {
        NoteActionsDialog(
            title = if (actionsTarget.size > 1) "${actionsTarget.size} notes selected" else "Note actions",
            onDismiss = { showActions = false },
            onAction = { action ->
                showActions = false
                when (action.label) {
                    "Pin" -> viewModel.togglePin(actionsTarget)
                    "Unpin" -> viewModel.togglePin(actionsTarget)
                    "Favorite" -> viewModel.toggleFavorite(actionsTarget)
                    "Unfavorite" -> viewModel.toggleFavorite(actionsTarget)
                    "Move to folder" -> showMoveToFolder = true
                    "Change color" -> showColorPicker = true
                    "Archive" -> viewModel.archiveNotes(actionsTarget)
                    "Unarchive" -> viewModel.unarchiveNotes(actionsTarget)
                    "Duplicate" -> viewModel.duplicateNotes(actionsTarget)
                    "Share" -> shareNotes(
                        context,
                        state.notes.filter { it.id in actionsTarget },
                    )
                    "Delete", "Delete forever" -> showDeleteConfirm = true
                }
            },
            actions = if (mode == ListMode.ARCHIVE) {
                listOf(
                    NoteAction("📤", "Unarchive"),
                    NoteAction("🗑️", "Delete forever", destructive = true),
                )
            } else {
                listOf(
                    NoteAction("📌", "Pin"),
                    NoteAction("❤️", "Favorite"),
                    NoteAction("📂", "Move to folder"),
                    NoteAction("🎨", "Change color"),
                    NoteAction("📚", "Archive"),
                    NoteAction("📄", "Duplicate"),
                    NoteAction("📤", "Share"),
                    NoteAction("🗑️", "Delete", destructive = true),
                )
            },
        )
    }

    if (showColorPicker) {
        val currentKey = state.selectedNotes.firstOrNull()?.colorKey
        NoteColorDialog(
            currentKey = currentKey,
            darkTheme = darkTheme,
            onDismiss = { showColorPicker = false },
            onPicked = {
                showColorPicker = false
                viewModel.setNoteColor(actionsTarget, it)
            },
        )
    }

    if (showMoveToFolder) {
        MoveToFolderDialog(
            title = "Move to folder",
            folders = state.folders,
            currentFolderId = state.selectedNotes.firstOrNull()?.folderId,
            onDismiss = { showMoveToFolder = false },
            onMove = { target ->
                showMoveToFolder = false
                viewModel.moveNotes(actionsTarget, target)
                viewModel.clearSelection()
            },
        )
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = if (actionsTarget.size > 1) "Delete ${actionsTarget.size} notes?" else "Delete note?",
            message = if (mode == ListMode.ARCHIVE) {
                "Notes will be permanently deleted. This cannot be undone."
            } else {
                "Deleted notes are moved to the Archive."
            },
            onConfirm = {
                showDeleteConfirm = false
                if (mode == ListMode.ARCHIVE) viewModel.deleteForever(actionsTarget) else viewModel.deleteNotes(actionsTarget)
                viewModel.clearSelection()
            },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}

private fun emptyTitle(mode: ListMode): String = when (mode) {
    ListMode.ALL -> "No notes yet"
    ListMode.FAVORITES -> "No favorites yet"
    ListMode.ARCHIVE -> "Archive is empty"
}

private fun emptySubtitle(mode: ListMode): String = when (mode) {
    ListMode.ALL -> "Tap New note to capture your first piece of information."
    ListMode.FAVORITES -> "Long-press a note and choose Favorite."
    ListMode.ARCHIVE -> "Archived notes appear here for safe keeping."
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    count: Int,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    onMore: () -> Unit,
) {
    TopAppBar(
        title = { Text("$count selected") },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Close selection")
            }
        },
        actions = {
            IconButton(onClick = onSelectAll) {
                Icon(Icons.Filled.Checklist, contentDescription = "Select all")
            }
            IconButton(onClick = onMore) {
                Icon(Icons.Filled.MoreHoriz, contentDescription = "More actions")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListTopBar(
    title: String,
    searchActive: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    showArchiveEntry: Boolean,
    onOpenArchive: () -> Unit,
    currentSort: SortOption,
    ascending: Boolean,
    onOptionSelected: (SortOption) -> Unit,
    onToggleDirection: () -> Unit,
) {
    var sortMenuOpen by remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            if (searchActive) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Search notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )
            } else {
                Text(title)
            }
        },
        actions = {
            IconButton(onClick = onSearchToggle) {
                Icon(
                    imageVector = if (searchActive) Icons.Filled.Close else Icons.Filled.Search,
                    contentDescription = "Search",
                )
            }
            Box {
                IconButton(onClick = { sortMenuOpen = true }) {
                    Icon(Icons.Filled.Sort, contentDescription = "Sort")
                }
                SortMenu(
                    expanded = sortMenuOpen,
                    current = currentSort,
                    ascending = ascending,
                    onDismiss = { sortMenuOpen = false },
                    onOptionSelected = {
                        sortMenuOpen = false
                        onOptionSelected(it)
                    },
                    onToggleDirection = {
                        onToggleDirection()
                    },
                )
            }
            if (showArchiveEntry) {
                IconButton(onClick = onOpenArchive) {
                    Icon(Icons.Filled.Archive, contentDescription = "Archive")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

private fun shareNotes(context: android.content.Context, notes: List<Note>) {
    if (notes.isEmpty()) return
    val text = notes.joinToString("\n\n---\n\n") { note ->
        buildString {
            if (note.title.isNotBlank()) appendLine(note.title)
            if (note.content.isNotBlank()) {
                if (isNotEmpty()) appendLine()
                append(note.content)
            }
        }
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share notes"))
}
