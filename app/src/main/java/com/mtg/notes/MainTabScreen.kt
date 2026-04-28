package com.mtg.notes

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.text.format.DateUtils

enum class BottomTab(val title: String, val icon: ImageVector) {
    LIST("Список", Icons.Default.List),
    GRID("Плитка", Icons.Default.GridView),
    PROFILE("Профіль", Icons.Default.Person)
}

@Composable
fun MainTabScreen(
    userName: String,
    globalNavController: NavController,
    mainViewModel: MainViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val isLoading by mainViewModel.isLoading.collectAsStateWithLifecycle()
    val notesToShow by mainViewModel.notesToShow.collectAsStateWithLifecycle()
    val searchQuery by mainViewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFolder by mainViewModel.selectedFolder.collectAsStateWithLifecycle()
    val activeFolders by mainViewModel.activeFolders.collectAsStateWithLifecycle()
    val folderCounts by mainViewModel.folderCounts.collectAsStateWithLifecycle()
    val totalNotesCount by mainViewModel.totalNotesCount.collectAsStateWithLifecycle()

    // DataStore
    val isSortAscending by mainViewModel.isSortAscending.collectAsStateWithLifecycle()
    val showFavoritesOnly by mainViewModel.showFavoritesOnly.collectAsStateWithLifecycle()
    val currentUserName by profileViewModel.userName.collectAsStateWithLifecycle()
    val isDarkTheme by profileViewModel.isDarkTheme.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableStateOf(BottomTab.LIST) }
    var showFolders by rememberSaveable { mutableStateOf(true) }

    var isFabExpanded by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var folderNameInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentTab != BottomTab.PROFILE && !isLoading) {
                MainFab(
                    isExpanded = isFabExpanded,
                    onToggle = { isFabExpanded = !isFabExpanded },
                    onCreateFolder = { isFabExpanded = false; showFolderDialog = true },
                    onCreateNote = {
                        isFabExpanded = false
                        mainViewModel.createNote { newId ->
                            globalNavController.navigate(Screen.NoteDetails.createRoute(newId))
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().statusBarsPadding()) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (currentTab) {
                    BottomTab.LIST -> {
                        ListTabContent(
                            notes = notesToShow,
                            totalNotesCount = totalNotesCount,
                            searchQuery = searchQuery,
                            onQueryChange = { mainViewModel.updateSearchQuery(it) },
                            folders = activeFolders,
                            counts = folderCounts,
                            selectedFolder = selectedFolder,
                            onFolderSelect = { mainViewModel.selectFolder(it) },
                            onNoteClick = { globalNavController.navigate(Screen.NoteDetails.createRoute(it.id)) },
                            onDeleteRequest = { noteToDelete = it },
                            onToggleFavorite = { mainViewModel.toggleFavorite(it) },
                            showFolders = showFolders,
                            onToggleFolders = {
                                showFolders = !showFolders
                                if (!showFolders) mainViewModel.selectFolder(null)
                            },
                            isSortAsc = isSortAscending,
                            onToggleSort = { mainViewModel.toggleSortOrder() },
                            showFavoritesOnly = showFavoritesOnly,
                            onToggleFavoritesFilter = { mainViewModel.toggleFavoritesOnly() }
                        )
                    }
                    BottomTab.GRID -> {
                        GridTabContent(
                            notes = notesToShow,
                            totalNotesCount = totalNotesCount,
                            folders = activeFolders,
                            counts = folderCounts,
                            selectedFolder = selectedFolder,
                            isSortAsc = isSortAscending,
                            onToggleSort = { mainViewModel.toggleSortOrder() },
                            onFolderSelect = { mainViewModel.selectFolder(it) },
                            onNoteClick = { globalNavController.navigate(Screen.NoteDetails.createRoute(it.id)) },
                            onDeleteRequest = { noteToDelete = it },
                            onToggleFavorite = { mainViewModel.toggleFavorite(it) },
                            showFolders = showFolders,
                            onToggleFolders = {
                                showFolders = !showFolders
                                if (!showFolders) mainViewModel.selectFolder(null)
                            },
                            showFavoritesOnly = showFavoritesOnly,
                            onToggleFavoritesFilter = { mainViewModel.toggleFavoritesOnly() }
                        )
                    }
                    BottomTab.PROFILE -> {
                        ProfileTab(
                            userName = currentUserName.ifEmpty { userName },
                            onNameChange = { profileViewModel.updateName(it) },
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = { profileViewModel.toggleTheme() },
                            isSortAscending = isSortAscending,
                            onToggleSort = { mainViewModel.toggleSortOrder() }
                        )
                    }
                }
            }
        }
    }

    if (noteToDelete != null) {
        DeleteConfirmationDialog(
            noteTitle = noteToDelete?.title ?: "",
            onConfirm = {
                mainViewModel.deleteNote(noteToDelete!!)
                noteToDelete = null
            },
            onDismiss = { noteToDelete = null }
        )
    }

    if (showFolderDialog) {
        AlertDialog(
            onDismissRequest = { showFolderDialog = false },
            title = { Text("Нова папка") },
            text = {
                OutlinedTextField(
                    value = folderNameInput,
                    onValueChange = { folderNameInput = it },
                    singleLine = true,
                    placeholder = { Text("Назва папки") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    Toast.makeText(context, "Симуляція: $folderNameInput", Toast.LENGTH_SHORT).show()
                    folderNameInput = ""; showFolderDialog = false
                }) { Text("Створити") }
            },
            dismissButton = {
                TextButton(onClick = { folderNameInput = ""; showFolderDialog = false }) { Text("Відміна") }
            }
        )
    }
}

@Composable
fun MainFab(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCreateFolder: () -> Unit,
    onCreateNote: () -> Unit
) {
    Column(horizontalAlignment = Alignment.End) {
        if (isExpanded) {
            SmallFloatingActionButton(
                onClick = onCreateFolder,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Folder, "Папка")
                    Spacer(Modifier.width(8.dp))
                    Text("Папка")
                }
            }
            SmallFloatingActionButton(
                onClick = onCreateNote,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, "Нотатка")
                    Spacer(Modifier.width(8.dp))
                    Text("Нотатка")
                }
            }
        }

        FloatingActionButton(
            onClick = onToggle,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = CircleShape
        ) {
            Icon(if (isExpanded) Icons.Default.Close else Icons.Default.Add, "Меню")
        }
    }
}

@Composable
fun ListTabContent(
    notes: List<Note>, totalNotesCount: Int, searchQuery: String, onQueryChange: (String) -> Unit,
    folders: Set<Folder>, counts: Map<Folder, Int>, selectedFolder: Folder?, onFolderSelect: (Folder?) -> Unit,
    onNoteClick: (Note) -> Unit, onDeleteRequest: (Note) -> Unit, onToggleFavorite: (Note) -> Unit,
    showFolders: Boolean, onToggleFolders: () -> Unit, isSortAsc: Boolean, onToggleSort: () -> Unit,
    showFavoritesOnly: Boolean, onToggleFavoritesFilter: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        NotesHeader(totalNotesCount > 5, showFolders, onToggleFolders, isSortAsc, onToggleSort, showFavoritesOnly, onToggleFavoritesFilter)

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            SearchBar(query = searchQuery, onQueryChange = onQueryChange)
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (showFolders) {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                item { FolderListItem("Всі", totalNotesCount, selectedFolder == null) { onFolderSelect(null) } }
                items(folders.toList()) { folder -> FolderListItem(folder.displayName, counts[folder] ?: 0, selectedFolder == folder) { onFolderSelect(folder) } }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (notes.isEmpty()) EmptyNotesPlaceholder()
        else {
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(notes) { note -> NoteListItem(note, { onNoteClick(note) }, { onDeleteRequest(note) }, { onToggleFavorite(note) }) }
            }
        }
    }
}

@Composable
fun GridTabContent(
    notes: List<Note>, totalNotesCount: Int, folders: Set<Folder>, counts: Map<Folder, Int>,
    selectedFolder: Folder?, isSortAsc: Boolean, onToggleSort: () -> Unit, onFolderSelect: (Folder?) -> Unit,
    onNoteClick: (Note) -> Unit, onDeleteRequest: (Note) -> Unit, onToggleFavorite: (Note) -> Unit,
    showFolders: Boolean, onToggleFolders: () -> Unit, showFavoritesOnly: Boolean, onToggleFavoritesFilter: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        NotesHeader(totalNotesCount > 5, showFolders, onToggleFolders, isSortAsc, onToggleSort, showFavoritesOnly, onToggleFavoritesFilter)

        if (showFolders) {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                item { FolderGridItem("Всі", totalNotesCount, selectedFolder == null) { onFolderSelect(null) } }
                items(folders.toList()) { folder -> FolderGridItem(folder.displayName, counts[folder] ?: 0, selectedFolder == folder) { onFolderSelect(folder) } }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (notes.isEmpty()) EmptyNotesPlaceholder()
        else {
            LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 160.dp), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(notes) { note -> NoteGridItem(note, { onNoteClick(note) }, { onDeleteRequest(note) }, { onToggleFavorite(note) }) }
            }
        }
    }
}

@Composable
fun NotesHeader(
    showLimitBadge: Boolean, showFolders: Boolean, onToggleFolders: () -> Unit,
    isSortAsc: Boolean, onToggleSort: () -> Unit,
    showFavoritesOnly: Boolean, onToggleFavoritesFilter: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Нотатки", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineMedium)
            if (showLimitBadge) {
                Spacer(Modifier.width(8.dp))
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp)) {
                    Text("Ліміт", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }
        Row {
            IconButton(onClick = onToggleFavoritesFilter) {
                Icon(
                    imageVector = if(showFavoritesOnly) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Тільки обрані",
                    tint = if(showFavoritesOnly) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onToggleSort) {
                Icon(Icons.Default.SortByAlpha, "Сортування", tint = if(isSortAsc) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
            }
            IconButton(onClick = onToggleFolders) {
                Icon(Icons.Default.Folder, "Папки", tint = if (showFolders) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun NoteGridItem(note: Note, onClick: () -> Unit, onDelete: () -> Unit, onToggleFavorite: () -> Unit) {
    val dateString = if (DateUtils.isToday(note.updatedAt)) SimpleDateFormat("HH:mm", Locale("uk", "UA")).format(Date(note.updatedAt)) else SimpleDateFormat("dd MMM", Locale("uk", "UA")).format(Date(note.updatedAt))
    Column(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { onClick() }.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Text(note.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))

            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(24.dp).padding(end = 4.dp)) {
                Icon(if (note.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder, "Обране", tint = if (note.isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(note.getPreviewText(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(dateString, color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall)
            Text(note.folder?.displayName ?: "", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun NoteListItem(note: Note, onClick: () -> Unit, onDelete: () -> Unit, onToggleFavorite: () -> Unit) {
    val dateString = if (DateUtils.isToday(note.updatedAt)) SimpleDateFormat("HH:mm", Locale("uk", "UA")).format(Date(note.updatedAt)) else SimpleDateFormat("dd MMM", Locale("uk", "UA")).format(Date(note.updatedAt))
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(note.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(note.getPreviewText(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(dateString, color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall)
            Text(note.folder?.displayName ?: "", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }

        IconButton(onClick = onToggleFavorite) {
            Icon(if (note.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder, "Обране", tint = if (note.isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun EmptyNotesPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "📝", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Створіть першу нотатку", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun DeleteConfirmationDialog(noteTitle: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = "Увага", tint = MaterialTheme.colorScheme.error) },
        title = { Text("Увага!") },
        text = { Text("Ви дійсно хочете видалити нотатку \"$noteTitle\"?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Так", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Ні") }
        }
    )
}

@Composable
fun ProfileTab(
    userName: String,
    onNameChange: (String) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    isSortAscending: Boolean,
    onToggleSort: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Профіль", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = onNameChange,
            label = { Text("Ваше ім'я") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))


        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Налаштування", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Темна тема")
                    Switch(checked = isDarkTheme, onCheckedChange = { onToggleTheme() })
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Сортування А-Я (за замовчуванням)")
                    Switch(checked = isSortAscending, onCheckedChange = { onToggleSort() })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Інформація", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Назва: Notes App (Лаба 8)")
                Text("Версія: 8.0.0")
                Text("Локальне сховище: Room + DataStore")
            }
        }
    }
}