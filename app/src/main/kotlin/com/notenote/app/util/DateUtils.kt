package com.notenote.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    fun formatDateTime(timestamp: Long, locale: Locale = Locale.getDefault()): String {
        val sdf = SimpleDateFormat("d MMM yyyy, HH:mm", locale)
        return sdf.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long, locale: Locale = Locale.getDefault()): String {
        val sdf = SimpleDateFormat("d MMM yyyy", locale)
        return sdf.format(Date(timestamp))
    }

    fun formatRelative(timestamp: Long, now: Long = System.currentTimeMillis()): String {
        val diff = now - timestamp
        val minute = 60_000L
        val hour = 60 * minute
        val day = 24 * hour
        return when {
            diff < minute -> "just now"
            diff < hour -> "${diff / minute} min ago"
            diff < day -> "${diff / hour} h ago"
            diff < 7 * day -> "${diff / day} d ago"
            else -> formatDate(timestamp)
        }
    }

    fun reminderLabel(timestamp: Long, now: Long = System.currentTimeMillis()): String = when {
        timestamp < now -> "Overdue · ${formatDateTime(timestamp)}"
        else -> formatDateTime(timestamp)
    }

    fun combineDateAndTime(dateMillis: Long, hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateMillis
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun splitDateTime(timestamp: Long): Pair<Long, IntPair> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val timeOfDay = IntPair(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        val date = (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return date.timeInMillis to timeOfDay
    }

    data class IntPair(val first: Int, val second: Int)
}
