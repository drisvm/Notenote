package com.notenote.app.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notenote.app.NotenoteApplication
import com.notenote.app.notification.Notifications
import kotlinx.coroutines.flow.first

class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val noteId = inputData.getLong(KEY_NOTE_ID, -1L)
        if (noteId <= 0L) return Result.success()
        val container = (applicationContext as NotenoteApplication).container
        val note = container.noteRepository.getById(noteId) ?: return Result.success()
        val reminderAt = note.reminderAt ?: return Result.success()

        if (reminderAt > System.currentTimeMillis() + 5_000L) {
            ReminderScheduler.schedule(applicationContext, noteId, reminderAt)
            return Result.success()
        }
        if (note.isArchived) return Result.success()

        Notifications.showReminder(
            context = applicationContext,
            noteId = note.id,
            title = note.displayTitle.ifBlank { "Note reminder" },
            body = note.content.take(160).ifBlank { "Time to check this note." },
        )
        return Result.success()
    }

    companion object {
        const val KEY_NOTE_ID = "note_id"
    }
}
