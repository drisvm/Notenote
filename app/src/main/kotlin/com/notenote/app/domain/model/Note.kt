package com.notenote.app.domain.model

data class Note(
    val id: Long = 0L,
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
) {
    val displayTitle: String get() = title.ifBlank { content.lineSequence().firstOrNull()?.take(48) ?: "" }
    val hasReminder: Boolean get() = reminderAt != null
}

enum class ListMode(val label: String) {
    ALL("All Notes"),
    FAVORITES("Favorites"),
    ARCHIVE("Archive"),
}
