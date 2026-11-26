package com.notenote.app.ui.screen.settings

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notenote.app.AppContainer
import com.notenote.app.data.preferences.AppSettings
import com.notenote.app.data.preferences.ThemeMode
import com.notenote.app.billing.PurchaseState
import com.notenote.app.ui.theme.DEFAULT_ACCENT_HEX
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val noteCount: Int = 0,
    val folderCount: Int = 0,
)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    private val messages = Channel<String>(capacity = 4)
    val messageFlow = messages.receiveAsFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        container.settingsRepository.settings,
        container.noteRepository.observeActive(),
        container.folderRepository.observeAll(),
    ) { settings, notes, folders ->
        SettingsUiState(
            settings = settings,
            noteCount = notes.size,
            folderCount = folders.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { container.settingsRepository.setThemeMode(mode) }
    }

    fun setAccentColorHex(hex: String?) {
        viewModelScope.launch { container.settingsRepository.setAccentColorHex(hex) }
    }

    fun setPaperTexture(enabled: Boolean) {
        viewModelScope.launch { container.settingsRepository.setPaperTextureEnabled(enabled) }
    }

    val purchaseState: StateFlow<PurchaseState> = container.purchaseManager.state

    fun refreshPurchases() {
        viewModelScope.launch { container.purchaseManager.refresh() }
    }

    fun removeAds(activity: Activity) {
        viewModelScope.launch { container.purchaseManager.launchPurchase(activity) }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                container.context.contentResolver.openOutputStream(uri)?.use { stream ->
                    container.backupManager.writeTo(stream)
                } ?: throw IllegalStateException("Cannot open destination")
                messages.send("Backup exported")
            } catch (t: Throwable) {
                messages.send("Export failed: ${t.message ?: t.javaClass.simpleName}")
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                container.context.contentResolver.openInputStream(uri)?.use { stream ->
                    val result = container.backupManager.readFrom(stream)
                    messages.send("Imported ${result.notesImported} notes, ${result.foldersImported} folders")
                } ?: throw IllegalStateException("Cannot open file")
            } catch (t: Throwable) {
                messages.send("Import failed: ${t.message ?: t.javaClass.simpleName}")
            }
        }
    }
}
