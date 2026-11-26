package com.notenote.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun ColorPickerDialog(
    title: String,
    presets: List<Color>,
    initialColor: Color?,
    onDismiss: () -> Unit,
    onPicked: (Color?) -> Unit,
    allowNone: Boolean = true,
) {
    var r by remember { mutableStateOf((initialColor?.red ?: 0.7f) * 255f) }
    var g by remember { mutableStateOf((initialColor?.green ?: 0.55f) * 255f) }
    var b by remember { mutableStateOf((initialColor?.blue ?: 0.45f) * 255f) }
    val custom = Color(
        red = (r.toInt() / 255f).coerceIn(0f, 1f),
        green = (g.toInt() / 255f).coerceIn(0f, 1f),
        blue = (b.toInt() / 255f).coerceIn(0f, 1f),
        alpha = 1f,
    )

    NotenoteAlertDialog(
        title = title,
        confirmLabel = "Done",
        onConfirm = { onPicked(custom) },
        onDismiss = onDismiss,
        dismissLabel = if (allowNone) "Remove color" else "Cancel",
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        ) {
            items(presets.size) { index ->
                val color = presets[index]
                Swatch(
                    color = color,
                    selected = custom == color,
                    onClick = {
                        r = color.red * 255f
                        g = color.green * 255f
                        b = color.blue * 255f
                    },
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(custom)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
            Text(
                "  Custom",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ChannelSlider("Red", r, { r = it }, Color(0xFFB36A5E))
        ChannelSlider("Green", g, { g = it }, Color(0xFF7C8B6F))
        ChannelSlider("Blue", b, { b = it }, Color(0xFF6E8CA0))
    }
}

@Composable
private fun ChannelSlider(label: String, value: Float, onChange: (Float) -> Unit, trackColor: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(
                activeTrackColor = trackColor,
                thumbColor = trackColor,
            ),
        )
    }
}

@Composable
private fun Swatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
    )
}

fun Color.toHex(): String {
    fun channel(value: Float) = (value.coerceIn(0f, 1f) * 255f).roundToInt()
    return "#%02X%02X%02X".format(channel(red), channel(green), channel(blue))
}
