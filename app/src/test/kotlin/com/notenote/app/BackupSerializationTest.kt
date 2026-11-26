package com.notenote.app

import com.notenote.app.data.backup.BackupFile
import com.notenote.app.data.backup.BackupFolder
import com.notenote.app.data.backup.BackupNote
import com.notenote.app.data.backup.toBackup
import com.notenote.app.domain.model.Note
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupSerializationTest {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    @Test
    fun `note to backup round trips through json`() {
        val note = Note(
            id = 7,
            title = "Washer filter",
            content = "Clean every 3 months",
            icon = "🌀",
            colorKey = "sage",
            folderId = 2,
            tags = listOf("Maintenance", "Appliance"),
            isPinned = true,
            isFavorite = false,
            isArchived = false,
            reminderAt = 1_700_000_000_000,
            createdAt = 1_699_000_000_000,
            modifiedAt = 1_700_000_000_000,
        )
        val encoded = json.encodeToString(note.toBackup())
        val decoded = json.decodeFromString(BackupNote.serializer(), encoded)
        assertEquals(note.id, decoded.id)
        assertEquals(note.title, decoded.title)
        assertEquals(note.tags, decoded.tags)
        assertEquals(note.reminderAt, decoded.reminderAt)
    }

    @Test
    fun `backup file round trips with folders`() {
        val file = BackupFile(
            exportedAt = 1L,
            folders = listOf(BackupFolder(id = 1, name = "Home", icon = "🏠")),
            notes = listOf(BackupNote(id = 1, title = "Wi-Fi", content = "password123")),
        )
        val encoded = json.encodeToString(BackupFile.serializer(), file)
        val decoded = json.decodeFromString(BackupFile.serializer(), encoded)
        assertEquals(file, decoded)
    }

    @Test
    fun `unknown keys in future backups are ignored`() {
        val raw = """
            {"app":"notenote","version":2,"exportedAt":5,
             "futureField":"ignored",
             "notes":[{"id":3,"title":"TT","futureNoteField":12}]}
        """.trimIndent()
        val decoded = json.decodeFromString(BackupFile.serializer(), raw)
        assertEquals("TT", decoded.notes.single().title)
        assertTrue(decoded.folders.isEmpty())
    }
}
