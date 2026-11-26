package com.notenote.app

import com.notenote.app.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class DateUtilsTest {

    private fun utcCalendar(): Calendar =
        Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US)

    @Test
    fun `combines date and time`() {
        val calendar = utcCalendar().apply {
            clear()
            set(2024, Calendar.MAY, 3, 0, 0, 0)
        }
        val combined = DateUtils.combineDateAndTime(calendar.timeInMillis, 15, 45)
        calendar.set(Calendar.HOUR_OF_DAY, 15)
        calendar.set(Calendar.MINUTE, 45)
        assertEquals(calendar.timeInMillis, combined)
    }

    @Test
    fun `splits date and time of day`() {
        val timestamp = DateUtils.combineDateAndTime(1_700_000_000_000L, 21, 30)
        val (date, time) = DateUtils.splitDateTime(timestamp)
        assertEquals(21, time.first)
        assertEquals(30, time.second)
        assertEquals(timestamp, DateUtils.combineDateAndTime(date, 21, 30))
    }

    @Test
    fun `relative labels for fresh timestamps`() {
        val now = System.currentTimeMillis()
        assertEquals("just now", DateUtils.formatRelative(now - 10_000))
        assertEquals("5 min ago", DateUtils.formatRelative(now - 5 * 60_000))
        assertEquals("3 h ago", DateUtils.formatRelative(now - 3 * 3_600_000))
        assertTrue(DateUtils.formatRelative(now - 10 * 86_400_000L).isNotBlank())
    }

    @Test
    fun `reminder label flags overdue`() {
        val now = System.currentTimeMillis()
        assertTrue(DateUtils.reminderLabel(now - 60_000).startsWith("Overdue"))
        assertTrue(!DateUtils.reminderLabel(now + 60_000).startsWith("Overdue"))
    }
}
