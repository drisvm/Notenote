package com.notenote.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.notenote.app.NotenoteApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReminderBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val container = (context.applicationContext as NotenoteApplication).container
                val now = System.currentTimeMillis()
                container.noteRepository.getAllWithReminders()
                    .filter { (it.reminderAt ?: 0L) > now }
                    .forEach { ReminderScheduler.schedule(context, it.id, it.reminderAt!!) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
