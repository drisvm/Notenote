package com.notenote.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
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

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String = "",
    val icon: String? = null,
    val colorKey: String? = null,
    val parentId: Long? = null,
    val sortIndex: Long = 0L,
    val createdAt: Long = 0L,
)
