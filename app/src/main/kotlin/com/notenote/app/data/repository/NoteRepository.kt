package com.notenote.app.data.repository

import com.notenote.app.data.local.dao.FolderDao
import com.notenote.app.data.local.dao.NoteDao
import com.notenote.app.data.local.entity.NoteEntity
import com.notenote.app.data.local.entity.FolderEntity
import com.notenote.app.domain.model.Folder
import com.notenote.app.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun NoteEntity.toDomain() = Note(
    id = id,
    title = title,
    content = content,
    icon = icon,
    colorKey = colorKey,
    folderId = folderId,
    tags = tags,
    isPinned = isPinned,
    isFavorite = isFavorite,
    isArchived = isArchived,
    reminderAt = reminderAt,
    createdAt = createdAt,
    modifiedAt = modifiedAt,
)

fun Note.toEntity() = NoteEntity(
    id = id,
    title = title,
    content = content,
    icon = icon,
    colorKey = colorKey,
    folderId = folderId,
    tags = tags,
    isPinned = isPinned,
    isFavorite = isFavorite,
    isArchived = isArchived,
    reminderAt = reminderAt,
    createdAt = createdAt,
    modifiedAt = modifiedAt,
)

fun FolderEntity.toDomain() = Folder(
    id = id,
    name = name,
    icon = icon,
    colorKey = colorKey,
    parentId = parentId,
    sortIndex = sortIndex,
    createdAt = createdAt,
)

fun Folder.toEntity() = FolderEntity(
    id = id,
    name = name,
    icon = icon,
    colorKey = colorKey,
    parentId = parentId,
    sortIndex = sortIndex,
    createdAt = createdAt,
)

class NoteRepository(private val noteDao: NoteDao) {

    fun observeActive(): Flow<List<Note>> = noteDao.observeActive().map { it.map(NoteEntity::toDomain) }
    fun observeArchived(): Flow<List<Note>> = noteDao.observeArchived().map { it.map(NoteEntity::toDomain) }
    fun observeFavorites(): Flow<List<Note>> = noteDao.observeFavorites().map { it.map(NoteEntity::toDomain) }
    fun observeByFolder(folderId: Long): Flow<List<Note>> =
        noteDao.observeByFolder(folderId).map { it.map(NoteEntity::toDomain) }

    fun observeWithReminders(): Flow<List<Note>> =
        noteDao.observeWithReminders().map { it.map(NoteEntity::toDomain) }

    suspend fun getById(id: Long): Note? = noteDao.getById(id)?.toDomain()
    suspend fun getAll(): List<Note> = noteDao.getAll().map(NoteEntity::toDomain)
    suspend fun getAllWithReminders(): List<Note> =
        noteDao.getAllWithReminders().map(NoteEntity::toDomain)

    suspend fun save(note: Note): Long {
        val updated = if (note.id == 0L) note.copy(createdAt = note.createdAt.ifAtZeroNow()) else note
        return if (updated.id == 0L) {
            noteDao.insert(updated.toEntity())
        } else {
            noteDao.update(updated.toEntity())
            updated.id
        }
    }

    suspend fun delete(note: Note) = noteDao.delete(note.toEntity())

    suspend fun countInFolder(folderId: Long): Int = noteDao.countInFolder(folderId)
    suspend fun detachNotesFromFolder(folderId: Long) = noteDao.detachNotesFromFolder(folderId)
    suspend fun moveNotes(fromFolderId: Long, toFolderId: Long?) =
        noteDao.moveNotes(fromFolderId, toFolderId)

    private fun Long.ifAtZeroNow(): Long = if (this == 0L) System.currentTimeMillis() else this
}

class FolderRepository(private val folderDao: FolderDao) {

    fun observeAll(): Flow<List<Folder>> = folderDao.observeAll().map { it.map(FolderEntity::toDomain) }
    suspend fun getById(id: Long): Folder? = folderDao.getById(id)?.toDomain()
    suspend fun getAll(): List<Folder> = folderDao.getAll().map(FolderEntity::toDomain)

    suspend fun save(folder: Folder): Long {
        val entity = folder.toEntity()
        return if (entity.id == 0L) {
            val nextIndex = (folderDao.maxSortIndex() ?: -1L) + 1
            folderDao.insert(entity.copy(sortIndex = nextIndex))
        } else {
            folderDao.update(entity)
            entity.id
        }
    }

    suspend fun delete(folderId: Long) = folderDao.delete(folderId)
}
