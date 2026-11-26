package com.notenote.app

import com.notenote.app.domain.model.Note
import com.notenote.app.domain.model.SortOption
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteSortingTest {

    private fun note(
        id: Long,
        title: String,
        modifiedAt: Long = 0,
        createdAt: Long = 0,
        reminderAt: Long? = null,
        pinned: Boolean = false,
    ) = Note(
        id = id,
        title = title,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        reminderAt = reminderAt,
        isPinned = pinned,
    )

    private fun sort(notes: List<Note>, option: SortOption, ascending: Boolean): List<Note> {
        val base: Comparator<Note> = when (option) {
            SortOption.MODIFIED -> compareBy { it.modifiedAt }
            SortOption.CREATED -> compareBy { it.createdAt }
            SortOption.TITLE -> compareBy { it.title.lowercase() }
            SortOption.REMINDER -> compareBy { it.reminderAt ?: Long.MAX_VALUE }
        }
        val ordered = if (ascending) base else base.reversed()
        return notes.sortedWith(compareByDescending<Note> { it.isPinned }.then(ordered))
    }

    @Test
    fun `pinned notes always come first`() {
        val notes = listOf(
            note(1, "B", modifiedAt = 10),
            note(2, "A", modifiedAt = 99, pinned = true),
        )
        assertEquals(2L, sort(notes, SortOption.MODIFIED, ascending = false).first().id)
    }

    @Test
    fun `modified descending default`() {
        val notes = listOf(
            note(1, "old", modifiedAt = 10),
            note(2, "new", modifiedAt = 20),
            note(3, "newest", modifiedAt = 30),
        )
        assertEquals(listOf(3L, 2L, 1L), sort(notes, SortOption.MODIFIED, ascending = false).map { it.id })
    }

    @Test
    fun `title ascending is case insensitive`() {
        val notes = listOf(note(1, "banana"), note(2, "Apple"), note(3, "cherry"))
        assertEquals(listOf(2L, 1L, 3L), sort(notes, SortOption.TITLE, ascending = true).map { it.id })
    }

    @Test
    fun `notes without reminders sort last`() {
        val notes = listOf(
            note(1, "no reminder"),
            note(2, "soonest", reminderAt = 100),
            note(3, "later", reminderAt = 200),
        )
        assertEquals(listOf(2L, 3L, 1L), sort(notes, SortOption.REMINDER, ascending = true).map { it.id })
    }
}
