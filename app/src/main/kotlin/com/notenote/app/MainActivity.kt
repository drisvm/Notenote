package com.notenote.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.notenote.app.data.preferences.ThemeMode
import com.notenote.app.ui.navigation.NotenoteNavHost
import com.notenote.app.ui.theme.NotenoteTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_OPEN_NOTE_ID = "open_note_id"
    }

    private val pendingNoteId = mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleNavigationIntent(intent)

        val app = application as NotenoteApplication
        val themeState = app.container.settingsRepository.settings
            .map { it.themeMode to it.accentColorHex }
            .stateIn(lifecycleScope, SharingStarted.Eagerly, ThemeMode.SYSTEM to null as String?)

        setContent {
            val themeStateValue by themeState.collectAsStateWithLifecycle()
            NotenoteTheme(themeMode = themeStateValue.first, accentColorHex = themeStateValue.second) {
                val navController = rememberNavController()
                NotenoteNavHost(navController = navController, pendingNoteId = pendingNoteId.value)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent?) {
        val noteId = intent?.getLongExtra(EXTRA_OPEN_NOTE_ID, -1L) ?: -1L
        if (noteId > 0L) {
            pendingNoteId.value = noteId
            intent?.removeExtra(EXTRA_OPEN_NOTE_ID)
        }
    }
}
