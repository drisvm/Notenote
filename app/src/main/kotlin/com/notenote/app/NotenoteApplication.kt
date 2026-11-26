package com.notenote.app

import android.app.Application
import com.notenote.app.data.backup.BackupManager
import com.notenote.app.data.local.NotenoteDatabase
import com.notenote.app.data.preferences.SettingsRepository
import com.notenote.app.data.repository.FolderRepository
import com.notenote.app.data.repository.NoteRepository
import com.notenote.app.notification.Notifications

class NotenoteApplication : Application() {

    val container: AppContainer by lazy {
        AppContainer(this).also { Notifications.ensureChannel(this) }
    }
}

class AppContainer(application: Application) {
    val context: Application = application
    private val database = NotenoteDatabase.get(application)
    val noteRepository = NoteRepository(database.noteDao())
    val folderRepository = FolderRepository(database.folderDao())
    val settingsRepository = SettingsRepository(application)
    val backupManager = BackupManager(database.noteDao(), database.folderDao())
}
