package com.notenote.app.data.local.converter

import androidx.room.TypeConverter

private const val SEPARATOR = '\u001F'

class TagListConverter {

    @TypeConverter
    fun fromTags(tags: List<String>): String = tags.joinToString(SEPARATOR.toString())

    @TypeConverter
    fun toTags(stored: String): List<String> =
        if (stored.isBlank()) emptyList() else stored.split(SEPARATOR).filter { it.isNotBlank() }
}
