package com.notenote.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.notenote.app.domain.model.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColorHex: String? = null,
    val paperTextureEnabled: Boolean = true,
    val sortOption: SortOption = SortOption.MODIFIED,
    val sortAscending: Boolean = false,
)

private val Context.dataStore by preferencesDataStore(name = "notenote_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val PAPER_TEXTURE = booleanPreferencesKey("paper_texture")
        val SORT_OPTION = stringPreferencesKey("sort_option")
        val SORT_ASCENDING = booleanPreferencesKey("sort_ascending")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            accentColorHex = prefs[Keys.ACCENT_COLOR]?.takeIf { it.isNotBlank() },
            paperTextureEnabled = prefs[Keys.PAPER_TEXTURE] ?: true,
            sortOption = prefs[Keys.SORT_OPTION]?.let { runCatching { SortOption.valueOf(it) }.getOrNull() }
                ?: SortOption.MODIFIED,
            sortAscending = prefs[Keys.SORT_ASCENDING] ?: false,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setAccentColorHex(hex: String?) {
        context.dataStore.edit { prefs ->
            if (hex.isNullOrBlank()) prefs.remove(Keys.ACCENT_COLOR) else prefs[Keys.ACCENT_COLOR] = hex
        }
    }

    suspend fun setPaperTextureEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PAPER_TEXTURE] = enabled }
    }

    suspend fun setSorting(option: SortOption, ascending: Boolean) {
        context.dataStore.edit {
            it[Keys.SORT_OPTION] = option.name
            it[Keys.SORT_ASCENDING] = ascending
        }
    }
}
