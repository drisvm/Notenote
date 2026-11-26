package com.notenote.app.ui.screen.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notenote.app.AppContainer
import com.notenote.app.data.preferences.AppSettings
import com.notenote.app.domain.model.Folder
import com.notenote.app.domain.model.ListMode
import com.notenote.app.domain.model.Note
import com.notenote.app.domain.model.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class Selection(val active: Boolean = false, val ids: Set<Long> = emptySet())

data class NoteListUiState(
    val isLoading: Boolean = true,
    val notes: List<Note> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val query: String = "",
    val searchActive: Boolean = false,
    val sortOption: SortOption = SortOption.MODIFIED,
    val sortAscending: Boolean = false,
    val selectionMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
) {
    val selectedNotes: List<Note> get() = notes.filter { it.id in selectedIds }
}

data class ListInputs(
    val notes: List<Note>,
    val folders: List<Folder>,
    val settings: AppSettings,
    val query: String,
    val searchActive: Boolean,
)

class NoteListViewModel(
    private val container: AppContainer,
    val mode: ListMode,
    val folderId: Long?,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val searchActive = MutableStateFlow(false)
    private val selection = MutableStateFlow(Selection())

    val folders: StateFlow<List<Folder>> = container.folderRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val sourceFlow = when (mode) {
        ListMode.ALL -> container.noteRepository.observeActive()
        ListMode.FAVORITES -> container.noteRepository.observeFavorites()
        ListMode.ARCHIVE -> container.noteRepository.observeArchived()
    }

    private val inputsFlow = combine(
        if (folderId != null) container.noteRepository.observeByFolder(folderId) else sourceFlow,
        folders,
        container.settingsRepository.settings,
        query,
        searchActive,
        ::ListInputs,
    )

    val uiState: StateFlow<NoteListUiState> = combine(inputsFlow, selection) { inputs, selectionState ->
        buildUiState(inputs, selectionState)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoteListUiState())

    private fun buildUiState(inputs: ListInputs, selectionState: Selection): NoteListUiState {
        val normalizedQuery = inputs.query.trim().lowercase()
        val filtered = if (normalizedQuery.isEmpty()) inputs.notes else inputs.notes.filter { note ->
            note.title.lowercase().contains(normalizedQuery) ||
                note.content.lowercase().contains(normalizedQuery) ||
                note.tags.any { it.lowercase().contains(normalizedQuery) }
        }
        return NoteListUiState(
            isLoading = false,
            notes = filtered.sortedWith(comparator(inputs.settings.sortOption, inputs.settings.sortAscending)),
            folders = inputs.folders,
            query = inputs.query,
            searchActive = inputs.searchActive,
            sortOption = inputs.settings.sortOption,
            sortAscending = inputs.settings.sortAscending,
            selectionMode = selectionState.active,
            selectedIds = selectionState.ids,
        )
    }

    private fun comparator(option: SortOption, ascending: Boolean): Comparator<Note> {
        val base: Comparator<Note> = when (option) {
            SortOption.MODIFIED -> compareBy { it.modifiedAt }
            SortOption.CREATED -> compareBy { it.createdAt }
            SortOption.TITLE -> compareBy { it.title.lowercase() }
            SortOption.REMINDER -> compareBy { it.reminderAt ?: Long.MAX_VALUE }
        }
        val ordered = if (ascending) base else base.reversed()
        return compareByDescending<Note> { it.isPinned }.then(ordered)
    }

    fun setQuery(value: String) {
        query.value = value
    }

    fun setSearchActive(active: Boolean) {
        searchActive.value = active
        if (!active) query.value = ""
    }

    fun updateSorting(option: SortOption, ascending: Boolean) {
        viewModelScope.launch { container.settingsRepository.setSorting(option, ascending) }
    }

    fun toggleSortDirection() {
        val current = uiState.value
        updateSorting(current.sortOption, !current.sortAscending)
    }

    fun enterSelection(initialId: Long) {
        selection.value = Selection(active = true, ids = setOf(initialId))
    }

    fun toggleSelected(noteId: Long) {
        val current = selection.value
        val updated = if (noteId in current.ids) current.ids - noteId else current.ids + noteId
        selection.value = Selection(active = updated.isNotEmpty(), ids = updated)
    }

    fun selectAll() {
        selection.value = Selection(active = true, ids = uiState.value.notes.map { it.id }.toSet())
    }

    fun clearSelection() {
        selection.value = Selection()
    }

    fun deleteNotes(ids: Collection<Long>) = mutate(ids) {
        it.copy(isArchived = true, modifiedAt = now())
    }

    fun deleteForever(ids: Collection<Long>) {
        viewModelScope.launch {
            ids.forEach { id ->
                container.noteRepository.getById(id)?.let { container.noteRepository.delete(it) }
            }
        }
    }

    fun restoreNotes(ids: Collection<Long>) = mutate(ids) {
        it.copy(isArchived = false, modifiedAt = now())
    }

    fun duplicateNotes(ids: Collection<Long>) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            ids.forEach { id ->
                val note = container.noteRepository.getById(id) ?: return@forEach
                container.noteRepository.save(
                    note.copy(
                        id = 0L,
                        title = note.title.plus(" (copy)").trim(),
                        isPinned = false,
                        createdAt = timestamp,
                        modifiedAt = timestamp,
                    ),
                )
            }
        }
    }

    fun moveNotes(ids: Collection<Long>, targetFolderId: Long?) =
        mutate(ids) { it.copy(folderId = targetFolderId, modifiedAt = now()) }

    fun togglePin(ids: Collection<Long>) = mutate(ids) { it.copy(isPinned = !it.isPinned, modifiedAt = now()) }

    fun toggleFavorite(ids: Collection<Long>) = mutate(ids) {
        it.copy(isFavorite = !it.isFavorite, modifiedAt = now())
    }

    fun archiveNotes(ids: Collection<Long>) = mutate(ids) { it.copy(isArchived = true, modifiedAt = now()) }

    fun unarchiveNotes(ids: Collection<Long>) = mutate(ids) { it.copy(isArchived = false, modifiedAt = now()) }

    fun setNoteColor(ids: Collection<Long>, colorKey: String?) = mutate(ids) {
        it.copy(colorKey = colorKey, modifiedAt = now())
    }

    private fun mutate(ids: Collection<Long>, transform: (Note) -> Note) {
        viewModelScope.launch {
            ids.forEach { id ->
                val note = container.noteRepository.getById(id) ?: return@forEach
                container.noteRepository.save(transform(note))
            }
        }
    }

    private fun now() = System.currentTimeMillis()
}
