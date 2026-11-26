package com.notenote.app.reminders

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun schedule(context: Context, noteId: Long, reminderAt: Long) {
        val delay = reminderAt - System.currentTimeMillis()
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay.coerceAtLeast(0L), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_NOTE_ID to noteId))
            .addTag(tagFor(noteId))
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("reminder_$noteId", ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context, noteId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("reminder_$noteId")
    }

    fun tagFor(noteId: Long) = "reminder_tag_$noteId"
}
