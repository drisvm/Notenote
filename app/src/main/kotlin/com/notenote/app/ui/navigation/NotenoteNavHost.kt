package com.notenote.app.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.notenote.app.domain.model.ListMode
import com.notenote.app.ui.screen.editor.NoteEditorScreen
import com.notenote.app.ui.screen.folders.FoldersScreen
import com.notenote.app.ui.screen.list.NoteListScreen
import com.notenote.app.ui.screen.reminders.RemindersScreen
import com.notenote.app.ui.screen.settings.SettingsScreen
import com.notenote.app.ui.theme.paperTexture

object Routes {
    const val ALL_NOTES = "notes/all"
    const val FAVORITES = "notes/favorites"
    const val ARCHIVE = "notes/archive"
    const val FOLDERS = "folders"
    const val REMINDERS = "reminders"
    const val SETTINGS = "settings"
    const val FOLDER_NOTES = "folder/{folderId}"
    const val EDITOR = "editor/{noteId}/{folderId}"

    fun folderNotes(folderId: Long) = "folder/$folderId"
    fun editor(noteId: Long = 0L, folderId: Long? = null) = "editor/$noteId/${folderId ?: -1L}"
}

private data class BottomDestination(
    val route: String,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val bottomDestinations = listOf(
    BottomDestination(Routes.ALL_NOTES, "Notes", Icons.Filled.EventNote, Icons.Outlined.EventNote),
    BottomDestination(Routes.FAVORITES, "Favorites", Icons.Filled.Star, Icons.Outlined.StarOutline),
    BottomDestination(Routes.FOLDERS, "Folders", Icons.Filled.Folder, Icons.Outlined.Folder),
    BottomDestination(Routes.REMINDERS, "Reminders", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    BottomDestination(Routes.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)

@Composable
fun NotenoteNavHost(navController: NavHostController, pendingNoteId: Long?) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val darkTheme = isSystemInDarkTheme()

    LaunchedEffect(pendingNoteId) {
        if (pendingNoteId != null && pendingNoteId > 0L) {
            navController.navigate(Routes.editor(pendingNoteId))
        }
    }

    Scaffold(
        modifier = Modifier.paperTexture(enabled = true, darkTheme = darkTheme),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute in bottomDestinations.map { it.route }) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    bottomDestinations.forEach { destination ->
                        val selected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    if (selected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.ALL_NOTES,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.ALL_NOTES) {
                NoteListScreen(
                    mode = ListMode.ALL,
                    folderId = null,
                    folderTitle = "All Notes",
                    onOpenNote = { navController.navigate(Routes.editor(it)) },
                    onCreateNote = { folderId -> navController.navigate(Routes.editor(folderId = folderId)) },
                    onOpenArchive = { navController.navigate(Routes.ARCHIVE) },
                )
            }
            composable(Routes.FAVORITES) {
                NoteListScreen(
                    mode = ListMode.FAVORITES,
                    folderId = null,
                    folderTitle = "Favorites",
                    onOpenNote = { navController.navigate(Routes.editor(it)) },
                    onCreateNote = { navController.navigate(Routes.editor()) },
                    onOpenArchive = {},
                )
            }
            composable(Routes.ARCHIVE) {
                NoteListScreen(
                    mode = ListMode.ARCHIVE,
                    folderId = null,
                    folderTitle = "Archive",
                    onOpenNote = { navController.navigate(Routes.editor(it)) },
                    onCreateNote = {},
                    onOpenArchive = {},
                )
            }
            composable(
                route = Routes.FOLDER_NOTES,
                arguments = listOf(navArgument("folderId") { type = NavType.LongType }),
            ) { entry ->
                val folderId = entry.arguments?.getLong("folderId") ?: return@composable
                NoteListScreen(
                    mode = ListMode.ALL,
                    folderId = folderId,
                    folderTitle = "Folder",
                    onOpenNote = { navController.navigate(Routes.editor(it)) },
                    onCreateNote = { navController.navigate(Routes.editor(folderId = folderId)) },
                    onOpenArchive = {},
                )
            }
            composable(Routes.FOLDERS) {
                FoldersScreen(onOpenFolder = { folderId, _ ->
                    navController.navigate(Routes.folderNotes(folderId))
                })
            }
            composable(Routes.REMINDERS) {
                RemindersScreen(onOpenNote = { navController.navigate(Routes.editor(it)) })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen()
            }
            composable(
                route = Routes.EDITOR,
                arguments = listOf(
                    navArgument("noteId") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("folderId") { type = NavType.LongType; defaultValue = -1L },
                ),
            ) { entry ->
                NoteEditorScreen(
                    noteId = entry.arguments?.getLong("noteId") ?: 0L,
                    presetFolderId = entry.arguments?.getLong("folderId")?.takeIf { it > 0 },
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
