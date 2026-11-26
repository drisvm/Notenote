package com.notenote.app.domain.model

data class Folder(
    val id: Long = 0L,
    val name: String = "",
    val icon: String? = null,
    val colorKey: String? = null,
    val parentId: Long? = null,
    val sortIndex: Long = 0L,
    val createdAt: Long = 0L,
)

enum class SortOption(val label: String) {
    MODIFIED("Last modified"),
    CREATED("Date created"),
    TITLE("Title"),
    REMINDER("Reminder date"),
}
