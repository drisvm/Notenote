package com.notenote.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class EmojiGroup(val title: String, val emojis: List<String>)

val NoteEmojiGroups = listOf(
    EmojiGroup(
        "Objects",
        listOf("📱", "💻", "🖥️", "🖨️", "📷", "🫖", "🧹", "🔧", "🔑", "🛠️", "🔌", "🔋", "💡", "🪛", "🧰", "📐", "📦", "🧾", "💳", "🎫", "🧳", "🎒", "🪪"),
    ),
    EmojiGroup(
        "Home",
        listOf("🏠", "🛏️", "🛋️", "🚿", "🛁", "🍳", "🍽️", "🧺", "🧴", "🧯", "🌀", "❄️", "🔥", "🌡️", "🕰️", "📺"),
    ),
    EmojiGroup(
        "Vehicles",
        listOf("🚗", "🚙", "🛻", "🏍️", "🚲", "🛵", "🚛", "🚐", "⛽", "🛞", "🅿️"),
    ),
    EmojiGroup(
        "Work & numbers",
        listOf("📋", "📌", "📎", "🗂️", "📁", "📅", "⏰", "⏱️", "🔢", "🏷️", "🔖", "🔐", "🗝️", "🧮", "✒️", "🧷"),
    ),
    EmojiGroup(
        "People & health",
        listOf("👤", "👨‍👩‍👧", "🧑", "⚖️", "💊", "🩺", "💉", "🦷", "👓", "👟", "👕", "🧑‍🍳"),
    ),
    EmojiGroup(
        "Nature & misc",
        listOf("🌱", "🌿", "🌳", "🌸", "🌾", "🐾", "🐈", "🐕", "⭐", "✨", "❤️", "🍀", "🌍", "🌊", "☀️", "🌙"),
    ),
)

@Composable
fun EmojiPickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onPicked: (String?) -> Unit,
) {
    NotenoteAlertDialog(
        title = title,
        confirmLabel = "Remove icon",
        onConfirm = { onPicked(null) },
        onDismiss = onDismiss,
        dismissLabel = "Cancel",
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
        ) {
            NoteEmojiGroups.forEach { group ->
                item(key = "header_${group.title}", span = { GridItemSpan(6) }) {
                    Text(
                        group.title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp, start = 4.dp),
                    )
                }
                items(group.emojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clickable { onPicked(emoji) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(emoji, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
