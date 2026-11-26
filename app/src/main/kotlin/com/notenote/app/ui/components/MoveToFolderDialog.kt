package com.notenote.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.notenote.app.domain.model.Folder

@Composable
fun MoveToFolderDialog(
    title: String,
    folders: List<Folder>,
    currentFolderId: Long?,
    onDismiss: () -> Unit,
    onMove: (Long?) -> Unit,
) {
    NotenoteAlertDialog(
        title = title,
        confirmLabel = "Done",
        onConfirm = onDismiss,
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            FolderTargetRow(
                label = "General notes (no folder)",
                selected = currentFolderId == null,
                leading = { Icon(Icons.Filled.FolderOff, contentDescription = null) },
                onClick = { onMove(null) },
            )
            folders.forEach { folder ->
                FolderTargetRow(
                    label = folder.name,
                    selected = currentFolderId == folder.id,
                    leading = {
                        Text(folder.icon ?: "📁", style = MaterialTheme.typography.titleMedium)
                    },
                    onClick = { onMove(folder.id) },
                )
            }
        }
    }
}

@Composable
private fun FolderTargetRow(
    label: String,
    selected: Boolean,
    leading: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        leading()
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
        )
        Checkbox(checked = selected, onCheckedChange = { onClick() })
    }
}
