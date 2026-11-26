package com.notenote.app.data.backup

import com.notenote.app.data.local.dao.FolderDao
import com.notenote.app.data.local.dao.NoteDao
import com.notenote.app.domain.model.Folder
import com.notenote.app.domain.model.Note
import com.notenote.app.data.repository.toDomain
import com.notenote.app.data.repository.toEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class BackupFolder(
    val id: Long,
    val name: String,
    val icon: String? = null,
    val colorKey: String? = null,
    val parentId: Long? = null,
    val sortIndex: Long = 0L,
    val createdAt: Long = 0L,
)

@Serializable
data class BackupNote(
    val id: Long,
    val title: String = "",
    val content: String = "",
    val icon: String? = null,
    val colorKey: String? = null,
    val folderId: Long? = null,
    val tags: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val reminderAt: Long? = null,
    val createdAt: Long = 0L,
    val modifiedAt: Long = 0L,
)

@Serializable
data class BackupFile(
    val app: String = "notenote",
    val version: Int = 1,
    val exportedAt: Long,
    val folders: List<BackupFolder> = emptyList(),
    val notes: List<BackupNote> = emptyList(),
)

fun Note.toBackup() = BackupNote(
    id = id, title = title, content = content, icon = icon, colorKey = colorKey,
    folderId = folderId, tags = tags, isPinned = isPinned, isFavorite = isFavorite,
    isArchived = isArchived, reminderAt = reminderAt, createdAt = createdAt, modifiedAt = modifiedAt,
)

fun Folder.toBackup() = BackupFolder(
    id = id, name = name, icon = icon, colorKey = colorKey,
    parentId = parentId, sortIndex = sortIndex, createdAt = createdAt,
)

data class ImportResult(val notesImported: Int, val foldersImported: Int)

class BackupManager(
    private val noteDao: NoteDao,
    private val folderDao: FolderDao,
) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    suspend fun writeTo(stream: OutputStream) {
        val folders = folderDao.getAll().map {
            Folder(id = it.id, name = it.name, icon = it.icon, colorKey = it.colorKey,
                parentId = it.parentId, sortIndex = it.sortIndex, createdAt = it.createdAt).toBackup()
        }
        val notes = noteDao.getAll().map { it.toDomain().toBackup() }
        val file = BackupFile(exportedAt = System.currentTimeMillis(), folders = folders, notes = notes)
        stream.use { it.write(json.encodeToString(BackupFile.serializer(), file).toByteArray(Charsets.UTF_8)) }
    }

    suspend fun readFrom(stream: InputStream): ImportResult {
        val text = stream.use { it.readBytes().toString(Charsets.UTF_8) }
        val file = json.decodeFromString(BackupFile.serializer(), text)
        file.folders.forEach { backup ->
            folderDao.insert(
                Folder(
                    id = backup.id, name = backup.name, icon = backup.icon, colorKey = backup.colorKey,
                    parentId = backup.parentId, sortIndex = backup.sortIndex, createdAt = backup.createdAt,
                ).toEntity()
            )
        }
        file.notes.forEach { backup ->
            noteDao.insert(
                Note(
                    id = backup.id, title = backup.title, content = backup.content, icon = backup.icon,
                    colorKey = backup.colorKey, folderId = backup.folderId, tags = backup.tags,
                    isPinned = backup.isPinned, isFavorite = backup.isFavorite, isArchived = backup.isArchived,
                    reminderAt = backup.reminderAt, createdAt = backup.createdAt, modifiedAt = backup.modifiedAt,
                ).toEntity()
            )
        }
        return ImportResult(file.notes.size, file.folders.size)
    }
}
