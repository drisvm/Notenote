package com.notenote.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.notenote.app.data.preferences.ThemeMode

private fun parseHex(hex: String): Color? = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrNull()

private fun lightScheme(accent: Color) = lightColorScheme(
    primary = accent,
    onPrimary = Color.White,
    primaryContainer = lerp(accent, Color.White, 0.78f),
    onPrimaryContainer = lerp(accent, Color.Black, 0.45f),
    secondary = lerp(accent, PaperLightOnBackground, 0.45f),
    onSecondary = Color.White,
    secondaryContainer = PaperLightSurfaceVariant,
    onSecondaryContainer = PaperLightOnBackground,
    tertiary = lerp(accent, PaperLightOnBackground, 0.25f),
    background = PaperLightBackground,
    onBackground = PaperLightOnBackground,
    surface = PaperLightSurface,
    onSurface = PaperLightOnBackground,
    surfaceVariant = PaperLightSurfaceVariant,
    onSurfaceVariant = PaperLightOnSurfaceVariant,
    outline = PaperLightOutline,
    outlineVariant = lerp(PaperLightOutline, Color.White, 0.4f),
    surfaceTint = accent.copy(alpha = 0.08f),
)

private fun darkScheme(accent: Color) = darkColorScheme(
    primary = lerp(accent, Color.White, 0.25f),
    onPrimary = lerp(accent, Color.Black, 0.6f),
    primaryContainer = lerp(accent, PaperDarkSurface, 0.55f),
    onPrimaryContainer = lerp(accent, Color.White, 0.65f),
    secondary = lerp(accent, PaperDarkOnBackground, 0.45f),
    onSecondary = PaperDarkBackground,
    secondaryContainer = PaperDarkSurfaceVariant,
    onSecondaryContainer = PaperDarkOnBackground,
    tertiary = lerp(accent, PaperDarkOnBackground, 0.25f),
    background = PaperDarkBackground,
    onBackground = PaperDarkOnBackground,
    surface = PaperDarkSurface,
    onSurface = PaperDarkOnBackground,
    surfaceVariant = PaperDarkSurfaceVariant,
    onSurfaceVariant = PaperDarkOnSurfaceVariant,
    outline = PaperDarkOutline,
    outlineVariant = lerp(PaperDarkOutline, Color.Black, 0.3f),
    surfaceTint = accent.copy(alpha = 0.12f),
)

@Composable
fun NotenoteTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentColorHex: String? = null,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val accent = accentColorHex
        ?.let { parseHex(it) }
        ?: parseHex(DEFAULT_ACCENT_HEX)
        ?: Color(0xFFB36A5E)

    MaterialTheme(
        colorScheme = if (darkTheme) darkScheme(accent) else lightScheme(accent),
        typography = NotenoteTypography,
        shapes = NotenoteShapes,
        content = content,
    )
}
