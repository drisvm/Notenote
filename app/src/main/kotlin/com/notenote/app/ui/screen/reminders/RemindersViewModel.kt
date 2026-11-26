package com.notenote.app.ui.screen.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notenote.app.AppContainer
import com.notenote.app.domain.model.Note
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReminderEntry(
    val note: Note,
    val overdue: Boolean,
)

data class RemindersUiState(
    val isLoading: Boolean = true,
    val entries: List<ReminderEntry> = emptyList(),
)

class RemindersViewModel(private val container: AppContainer) : ViewModel() {

    val uiState: StateFlow<RemindersUiState> = container.noteRepository.observeWithReminders()
        .map { notes ->
            val now = System.currentTimeMillis()
            RemindersUiState(
                isLoading = false,
                entries = notes
                    .sortedBy { it.reminderAt ?: Long.MAX_VALUE }
                    .map { ReminderEntry(it, (it.reminderAt ?: 0L) < now) },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RemindersUiState())

    fun removeReminder(noteId: Long) {
        viewModelScope.launch {
            val note = container.noteRepository.getById(noteId) ?: return@launch
            container.noteRepository.save(note.copy(reminderAt = null))
            com.notenote.app.reminders.ReminderScheduler.cancel(container.context, noteId)
        }
    }
}
