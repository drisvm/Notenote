package com.notenote.app.ui.theme

import androidx.compose.ui.graphics.Color

// Warm paper palette - light mode
val PaperLightBackground = Color(0xFFF7F2E9)
val PaperLightSurface = Color(0xFFFCF8F0)
val PaperLightSurfaceVariant = Color(0xFFEFE7D8)
val PaperLightOutline = Color(0xFFD9CFBE)
val PaperLightOnBackground = Color(0xFF2E2A26)
val PaperLightOnSurfaceVariant = Color(0xFF5C564D)

// Warm charcoal palette - dark mode
val PaperDarkBackground = Color(0xFF201D1A)
val PaperDarkSurface = Color(0xFF2A2622)
val PaperDarkSurfaceVariant = Color(0xFF37322C)
val PaperDarkOutline = Color(0xFF4A443C)
val PaperDarkOnBackground = Color(0xFFEDE6DA)
val PaperDarkOnSurfaceVariant = Color(0xFFBDB4A6)

// Predefined accent colors
data class AccentOption(val name: String, val hex: String)

val AccentPalette = listOf(
    AccentOption("Terracotta", "#B36A5E"),
    AccentOption("Sage", "#7C8B6F"),
    AccentOption("Dusty Blue", "#6E8CA0"),
    AccentOption("Plum", "#8B6D8F"),
    AccentOption("Ochre", "#B08D45"),
    AccentOption("Cocoa", "#8D5B4C"),
)

const val DEFAULT_ACCENT_HEX = "#B36A5E"

// Per-note color keys; null key means the default paper color.
data class NoteColorSwatch(val key: String, val label: String, val light: Color, val dark: Color)

val NoteColorPalette = listOf(
    NoteColorSwatch("paper", "Paper", Color(0xFFF6EFE2), Color(0xFF332E28)),
    NoteColorSwatch("sage", "Sage", Color(0xFFDCE5D0), Color(0xFF323A2E)),
    NoteColorSwatch("dusty_blue", "Dusty Blue", Color(0xFFD7E3EA), Color(0xFF2C3740)),
    NoteColorSwatch("terracotta", "Terracotta", Color(0xFFF3DCD2), Color(0xFF40302A)),
    NoteColorSwatch("lavender", "Lavender", Color(0xFFE4DDEF), Color(0xFF352F42)),
    NoteColorSwatch("sand", "Sand", Color(0xFFF0E4C8), Color(0xFF3B362B)),
    NoteColorSwatch("rose", "Rose", Color(0xFFF4DBDD), Color(0xFF3F2E31)),
    NoteColorSwatch("olive", "Olive", Color(0xFFE7E9CE), Color(0xFF37392A)),
)

fun noteContainerColor(colorKey: String?, darkTheme: Boolean): Color =
    NoteColorPalette.firstOrNull { it.key == colorKey }
        ?.let { if (darkTheme) it.dark else it.light }
        ?: if (darkTheme) PaperDarkSurface else PaperLightSurface
