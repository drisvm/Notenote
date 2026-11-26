package com.notenote.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.notenote.app.ui.theme.NoteColorPalette

@Composable
fun NoteColorDialog(
    currentKey: String?,
    darkTheme: Boolean,
    onDismiss: () -> Unit,
    onPicked: (String?) -> Unit,
) {
    NotenoteAlertDialog(
        title = "Change color",
        confirmLabel = "Default",
        onConfirm = { onPicked(null) },
        onDismiss = onDismiss,
        dismissLabel = "Cancel",
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NoteColorPalette.forEach { swatch ->
                val color = if (darkTheme) swatch.dark else swatch.light
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (currentKey == swatch.key) 3.dp else 1.dp,
                            color = if (currentKey == swatch.key) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            shape = CircleShape,
                        )
                        .clickable { onPicked(swatch.key) },
                )
            }
        }
    }
}
