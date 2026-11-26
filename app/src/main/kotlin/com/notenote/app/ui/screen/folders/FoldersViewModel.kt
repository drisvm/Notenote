package com.notenote.app.ui.screen.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notenote.app.AppContainer
import com.notenote.app.domain.model.Folder
import com.notenote.app.ui.theme.noteContainerColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FolderWithCount(val folder: Folder, val noteCount: Int, val childCount: Int)

data class FoldersUiState(
    val isLoading: Boolean = true,
    val folders: List<FolderWithCount> = emptyList(),
)

sealed class FolderDeletionChoice {
    data object DetachNotes : FolderDeletionChoice()
    data class MoveToFolder(val targetFolderId: Long?) : FolderDeletionChoice()
}

class FoldersViewModel(private val container: AppContainer) : ViewModel() {

    private val refresh = MutableStateFlow(0)

    val uiState: StateFlow<FoldersUiState> = combine(
        container.folderRepository.observeAll(),
        refresh,
    ) { folders, _ ->
        val activeNotes = container.noteRepository.getAll().filterNot { it.isArchived }
        val withCounts = folders.map { folder ->
            FolderWithCount(
                folder = folder,
                noteCount = activeNotes.count { it.folderId == folder.id },
                childCount = folders.count { it.parentId == folder.id },
            )
        }
        FoldersUiState(isLoading = false, folders = withCounts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FoldersUiState())

    fun saveFolder(name: String, icon: String?, colorKey: String?, parentId: Long?, folderId: Long = 0L) {
        viewModelScope.launch {
            if (folderId == 0L) {
                if (name.isNotBlank()) {
                    container.folderRepository.save(
                        Folder(name = name, icon = icon, colorKey = colorKey, parentId = parentId),
                    )
                }
            } else {
                val existing = container.folderRepository.getById(folderId) ?: return@launch
                container.folderRepository.save(
                    existing.copy(
                        name = name.ifBlank { existing.name },
                        icon = icon,
                        colorKey = colorKey,
                    ),
                )
            }
            refresh.value++
        }
    }

    fun deleteFolder(folderId: Long, choice: FolderDeletionChoice) {
        viewModelScope.launch {
            when (choice) {
                FolderDeletionChoice.DetachNotes ->
                    container.noteRepository.moveNotes(folderId, null)
                is FolderDeletionChoice.MoveToFolder ->
                    container.noteRepository.moveNotes(folderId, choice.targetFolderId)
            }
            container.folderRepository.getAll()
                .filter { it.parentId == folderId }
                .forEach { container.folderRepository.save(it.copy(parentId = null)) }
            container.folderRepository.delete(folderId)
            refresh.value++
        }
    }

    companion object {
        val fallbackColor = noteContainerColor(null, false)
    }
}
