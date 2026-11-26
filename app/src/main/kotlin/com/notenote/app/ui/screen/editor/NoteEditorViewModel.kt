package com.notenote.app.ui.screen.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notenote.app.AppContainer
import com.notenote.app.domain.model.Folder
import com.notenote.app.domain.model.Note
import com.notenote.app.reminders.ReminderScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class EditorUiState(
    val note: Note? = null,
    val isNew: Boolean = false,
    val folders: List<Folder> = emptyList(),
    val savedOnce: Boolean = false,
)

class NoteEditorViewModel(
    private val container: AppContainer,
    private val noteId: Long,
    presetFolderId: Long?,
) : ViewModel() {

    private val _state = MutableStateFlow(EditorUiState(isNew = noteId == 0L))
    val state: StateFlow<EditorUiState> = _state

    private val draft = MutableStateFlow<Note?>(null)
    private var originalReminderAt: Long? = null
    private var autosaveJob: Job? = null
    private var loaded = false

    init {
        viewModelScope.launch {
            val folders = container.folderRepository.observeAll().first()
            val existing = if (noteId == 0L) null else container.noteRepository.getById(noteId)
            val base = existing ?: Note(
                folderId = presetFolderId,
                createdAt = System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis(),
            )
            originalReminderAt = existing?.reminderAt
            loaded = true
            draft.value = base
            _state.value = EditorUiState(
                note = base,
                isNew = existing == null,
                folders = folders,
                savedOnce = false,
            )
        }
    }

    fun update(transform: (Note) -> Note) {
        val current = draft.value ?: return
        val updated = transform(current).copy(modifiedAt = System.currentTimeMillis())
        draft.value = updated
        _state.value = _state.value.copy(note = updated)
        scheduleAutosave()
    }

    private fun scheduleAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(700)
            persist()
        }
    }

    fun persistNow() {
        autosaveJob?.cancel()
        kotlinx.coroutines.runBlocking { persist() }
    }

    private suspend fun persist() {
        val toSave = draft.value ?: return
        if (toSave.id == 0L && toSave.title.isBlank() && toSave.content.isBlank()) return
        val savedId = container.noteRepository.save(toSave)
        val saved = toSave.copy(id = savedId)
        draft.value = saved
        _state.value = _state.value.copy(note = saved, savedOnce = true, isNew = false)

        val scheduledReminder = saved.reminderAt
        when {
            scheduledReminder == null && originalReminderAt != null ->
                ReminderScheduler.cancel(container.context, savedId)
            scheduledReminder != null && scheduledReminder != originalReminderAt &&
                scheduledReminder > System.currentTimeMillis() ->
                ReminderScheduler.schedule(container.context, savedId, scheduledReminder)
            scheduledReminder != null && scheduledReminder <= System.currentTimeMillis() ->
                ReminderScheduler.cancel(container.context, savedId)
        }
        originalReminderAt = scheduledReminder
    }

    fun duplicateCurrent(onDone: () -> Unit) {
        viewModelScope.launch {
            val source = draft.value ?: return@launch
            val timestamp = System.currentTimeMillis()
            container.noteRepository.save(
                source.copy(
                    id = 0L,
                    title = source.title.plus(" (copy)").trim(),
                    isPinned = false,
                    createdAt = timestamp,
                    modifiedAt = timestamp,
                ),
            )
            onDone()
        }
    }

    fun archiveCurrent() {
        viewModelScope.launch {
            persist()
            val note = draft.value ?: return@launch
            if (note.id != 0L) {
                container.noteRepository.save(note.copy(isArchived = true, modifiedAt = System.currentTimeMillis()))
            }
        }
    }

    override fun onCleared() {
        if (loaded && draft.value != null) {
            autosaveJob?.cancel()
            kotlinx.coroutines.runBlocking { persist() }
        }
        super.onCleared()
    }
}
