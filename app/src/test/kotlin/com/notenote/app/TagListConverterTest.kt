package com.notenote.app

import com.notenote.app.data.local.converter.TagListConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class TagListConverterTest {

    private val converter = TagListConverter()

    @Test
    fun `round trips empty list`() {
        assertEquals(emptyList<String>(), converter.toTags(converter.fromTags(emptyList())))
    }

    @Test
    fun `round trips multiple tags`() {
        val tags = listOf("Warranty", "Kitchen", "Model 2024")
        assertEquals(tags, converter.toTags(converter.fromTags(tags)))
    }

    @Test
    fun `tags containing commas survive`() {
        val tags = listOf("a,b", "plain")
        assertEquals(tags, converter.toTags(converter.fromTags(tags)))
    }
}
