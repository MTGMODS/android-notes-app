package com.mtg.notes

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import java.io.File
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
import androidx.compose.ui.zIndex
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.text.format.DateUtils
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import kotlin.math.roundToInt
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch

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
    profileViewModel: ProfileViewModel = viewModel(),
    windowSizeClass: WindowSizeClass
) {

    val isExpandedScreen = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    var selectedNoteIdForPane by rememberSaveable { mutableStateOf<Int?>(null) }

    val isRefreshing by mainViewModel.isRefreshing.collectAsStateWithLifecycle()
    val isLoading by mainViewModel.isLoading.collectAsStateWithLifecycle()
    val isOffline by mainViewModel.isOffline.collectAsStateWithLifecycle()
    val notesToShow by mainViewModel.notesToShow.collectAsStateWithLifecycle()
    val searchQuery by mainViewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFolder by mainViewModel.selectedFolder.collectAsStateWithLifecycle()
    val activeFolders by mainViewModel.activeFolders.collectAsStateWithLifecycle()
    val folderCounts by mainViewModel.folderCounts.collectAsStateWithLifecycle()
    val totalNotesCount by mainViewModel.totalNotesCount.collectAsStateWithLifecycle()

    val isSortAscending by mainViewModel.isSortAscending.collectAsStateWithLifecycle()
    val showFavoritesOnly by mainViewModel.showFavoritesOnly.collectAsStateWithLifecycle()
    val currentUserName by profileViewModel.userName.collectAsStateWithLifecycle()
    val isDarkTheme by profileViewModel.isDarkTheme.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableStateOf(BottomTab.LIST) }
    var showFolders by rememberSaveable { mutableStateOf(true) }

    var isFabExpanded by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var showFolderDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        mainViewModel.errorMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    val handleNoteClick: (Int) -> Unit = { id ->
        if (isExpandedScreen && currentTab != BottomTab.PROFILE) {
            selectedNoteIdForPane = id
        } else {
            globalNavController.navigate(Screen.NoteDetails.createRoute(id))
        }
    }

    val handleCreateNote = {
        isFabExpanded = false
        if (isExpandedScreen && currentTab != BottomTab.PROFILE) {
            selectedNoteIdForPane = -1
        } else {
            globalNavController.navigate(Screen.NoteDetails.createRoute(-1))
        }
    }

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
                    onCreateNote = handleCreateNote
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().statusBarsPadding()) {

            Row(modifier = Modifier.fillMaxSize()) {

                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (isOffline) {
                        OfflineBanner(onRetry = { mainViewModel.refreshData() })
                    }

                    when (currentTab) {
                        BottomTab.LIST -> {
                            ListTabContent(
                                notes = notesToShow, totalNotesCount = totalNotesCount, searchQuery = searchQuery,
                                onQueryChange = { mainViewModel.updateSearchQuery(it) }, folders = activeFolders,
                                counts = folderCounts, selectedFolder = selectedFolder,
                                onFolderSelect = { mainViewModel.selectFolder(it) },
                                onNoteClick = { handleNoteClick(it.id) },
                                onDeleteRequest = { noteToDelete = it }, onToggleFavorite = { mainViewModel.toggleFavorite(it) },
                                showFolders = showFolders, onToggleFolders = {
                                    showFolders = !showFolders
                                    if (!showFolders) mainViewModel.selectFolder(null)
                                },
                                isSortAsc = isSortAscending, onToggleSort = { mainViewModel.toggleSortOrder() },
                                showFavoritesOnly = showFavoritesOnly, onToggleFavoritesFilter = { mainViewModel.toggleFavoritesOnly() },
                                isRefreshing = isRefreshing, onRefresh = { mainViewModel.refreshData(isSwipe = true) }
                            )
                        }
                        BottomTab.GRID -> {
                            GridTabContent(
                                notes = notesToShow, totalNotesCount = totalNotesCount, folders = activeFolders,
                                counts = folderCounts, selectedFolder = selectedFolder,
                                isSortAsc = isSortAscending, onToggleSort = { mainViewModel.toggleSortOrder() },
                                onFolderSelect = { mainViewModel.selectFolder(it) },
                                onNoteClick = { handleNoteClick(it.id) },
                                onDeleteRequest = { noteToDelete = it }, onToggleFavorite = { mainViewModel.toggleFavorite(it) },
                                showFolders = showFolders, onToggleFolders = {
                                    showFolders = !showFolders
                                    if (!showFolders) mainViewModel.selectFolder(null)
                                },
                                showFavoritesOnly = showFavoritesOnly, onToggleFavoritesFilter = { mainViewModel.toggleFavoritesOnly() },
                                isRefreshing = isRefreshing, onRefresh = { mainViewModel.refreshData(isSwipe = true) }
                            )
                        }
                        BottomTab.PROFILE -> {
                            ProfileTab(
                                userName = currentUserName.ifEmpty { userName }, onNameChange = { profileViewModel.updateName(it) },
                                isDarkTheme = isDarkTheme, onToggleTheme = { profileViewModel.toggleTheme() },
                                isSortAscending = isSortAscending, onToggleSort = { mainViewModel.toggleSortOrder() }
                            )
                        }
                    }
                }

                if (isExpandedScreen && currentTab != BottomTab.PROFILE) {
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Box(modifier = Modifier.weight(1.5f).fillMaxHeight()) {
                        if (selectedNoteIdForPane != null) {
                            NoteEditorOverlay(
                                noteId = selectedNoteIdForPane!!,
                                onExit = { selectedNoteIdForPane = null },
                                windowSizeClass = windowSizeClass
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📝", style = MaterialTheme.typography.displayMedium)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Оберіть нотатку для перегляду", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)).zIndex(10f).clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
}

@Composable
fun OfflineBanner(onRetry: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.errorContainer).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Офлайн режим. Кешовані дані.", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onRetry) {
            Text("Повторити", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MainFab(isExpanded: Boolean, onToggle: () -> Unit, onCreateFolder: () -> Unit, onCreateNote: () -> Unit) {
    Column(horizontalAlignment = Alignment.End) {
        if (isExpanded) {
            SmallFloatingActionButton(onClick = onCreateFolder, containerColor = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.padding(bottom = 8.dp)) {
                Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Folder, "Папка"); Spacer(Modifier.width(8.dp)); Text("Папка")
                }
            }
            SmallFloatingActionButton(onClick = onCreateNote, containerColor = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.padding(bottom = 16.dp)) {
                Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, "Нотатка"); Spacer(Modifier.width(8.dp)); Text("Нотатка")
                }
            }
        }
        FloatingActionButton(onClick = onToggle, containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer, shape = CircleShape) {
            Icon(if (isExpanded) Icons.Default.Close else Icons.Default.Add, "Меню")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListTabContent(
    notes: List<Note>, totalNotesCount: Int, searchQuery: String, onQueryChange: (String) -> Unit,
    folders: Set<Folder>, counts: Map<Folder, Int>, selectedFolder: Folder?, onFolderSelect: (Folder?) -> Unit,
    onNoteClick: (Note) -> Unit, onDeleteRequest: (Note) -> Unit, onToggleFavorite: (Note) -> Unit,
    showFolders: Boolean, onToggleFolders: () -> Unit, isSortAsc: Boolean, onToggleSort: () -> Unit,
    showFavoritesOnly: Boolean, onToggleFavoritesFilter: () -> Unit,
    isRefreshing: Boolean, onRefresh: () -> Unit
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
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                    items(notes, key = { it.id }) { note ->
                        NoteListItem(note, { onNoteClick(note) }, { onDeleteRequest(note) }, { onToggleFavorite(note) }, modifier = Modifier.animateItem())
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridTabContent(
    notes: List<Note>, totalNotesCount: Int, folders: Set<Folder>, counts: Map<Folder, Int>,
    selectedFolder: Folder?, isSortAsc: Boolean, onToggleSort: () -> Unit, onFolderSelect: (Folder?) -> Unit,
    onNoteClick: (Note) -> Unit, onDeleteRequest: (Note) -> Unit, onToggleFavorite: (Note) -> Unit,
    showFolders: Boolean, onToggleFolders: () -> Unit, showFavoritesOnly: Boolean, onToggleFavoritesFilter: () -> Unit,
    isRefreshing: Boolean, onRefresh: () -> Unit
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
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 160.dp), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                    items(notes, key = { it.id }) { note ->
                        NoteGridItem(note, { onNoteClick(note) }, { onDeleteRequest(note) }, { onToggleFavorite(note) }, modifier = Modifier.animateItem())
                    }
                }
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NoteGridItem(note: Note, onClick: () -> Unit, onDelete: () -> Unit, onToggleFavorite: () -> Unit, modifier: Modifier = Modifier) {
    val dateString = if (DateUtils.isToday(note.updatedAt)) SimpleDateFormat("HH:mm", Locale("uk", "UA")).format(Date(note.updatedAt)) else SimpleDateFormat("dd MMM", Locale("uk", "UA")).format(Date(note.updatedAt))
    var showMenu by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                onDelete()
                false
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val color by animateColorAsState(if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) MaterialTheme.colorScheme.errorContainer else Color.Transparent)
            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(color).padding(16.dp), contentAlignment = Alignment.Center) {
                if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) Icon(Icons.Default.Delete, "Видалити", tint = MaterialTheme.colorScheme.error)
            }
        }
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = { showMenu = true }
                    )
                    .padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Text(note.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    if (note.isFavorite) Icon(Icons.Default.Star, "Обране", tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(note.getPreviewText(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(dateString, color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall)
                    Text(note.folder?.displayName ?: "", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(text = { Text("Відкрити") }, onClick = { showMenu = false; onClick() }, leadingIcon = { Icon(Icons.Default.Edit, null) })
                DropdownMenuItem(text = { Text(if (note.isFavorite) "Видалити з обраного" else "Додати в обране") }, onClick = { showMenu = false; onToggleFavorite() }, leadingIcon = { Icon(if (note.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder, null) })
                DropdownMenuItem(text = { Text("Видалити", color = MaterialTheme.colorScheme.error) }, onClick = { showMenu = false; onDelete() }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NoteListItem(note: Note, onClick: () -> Unit, onDelete: () -> Unit, onToggleFavorite: () -> Unit, modifier: Modifier = Modifier) {
    val dateString = if (DateUtils.isToday(note.updatedAt)) SimpleDateFormat("HH:mm", Locale("uk", "UA")).format(Date(note.updatedAt)) else SimpleDateFormat("dd MMM", Locale("uk", "UA")).format(Date(note.updatedAt))

    var showMenu by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                onDelete()
                false
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val color by animateColorAsState(if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) MaterialTheme.colorScheme.errorContainer else Color.Transparent)
            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(color).padding(16.dp), contentAlignment = Alignment.CenterEnd) {
                if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) Icon(Icons.Default.Delete, "Видалити", tint = MaterialTheme.colorScheme.error)
            }
        }
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = { showMenu = true }
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Text(note.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(note.getPreviewText(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    if (note.isFavorite) Icon(Icons.Default.Star, "Обране", tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                    Text(dateString, color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall)
                    Text(note.folder?.displayName ?: "", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }

            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(text = { Text("Відкрити") }, onClick = { showMenu = false; onClick() }, leadingIcon = { Icon(Icons.Default.Edit, null) })
                DropdownMenuItem(text = { Text(if (note.isFavorite) "Видалити з обраного" else "Додати в обране") }, onClick = { showMenu = false; onToggleFavorite() }, leadingIcon = { Icon(if (note.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder, null) })
                DropdownMenuItem(text = { Text("Видалити", color = MaterialTheme.colorScheme.error) }, onClick = { showMenu = false; onDelete() }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) })
            }
        }
    }
}

@Composable
fun EmptyNotesPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("📝", style = MaterialTheme.typography.headlineMedium); Spacer(modifier = Modifier.height(16.dp)); Text("Створіть першу нотатку", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium) }
    }
}

@Composable
fun DeleteConfirmationDialog(noteTitle: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, icon = { Icon(Icons.Default.Warning, "Увага", tint = MaterialTheme.colorScheme.error) }, title = { Text("Увага!") }, text = { Text("Ви дійсно хочете видалити нотатку \"$noteTitle\"?") }, confirmButton = { TextButton(onClick = onConfirm) { Text("Так", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Ні") } })
}

@Composable
fun ProfileTab(userName: String, onNameChange: (String) -> Unit, isDarkTheme: Boolean, onToggleTheme: () -> Unit, isSortAscending: Boolean, onToggleSort: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Профіль", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(value = userName, onValueChange = onNameChange, label = { Text("Ваше ім'я") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(32.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Налаштування", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) { Text("Темна тема"); Switch(checked = isDarkTheme, onCheckedChange = { onToggleTheme() }) }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) { Text("Сортування А-Я"); Switch(checked = isSortAscending, onCheckedChange = { onToggleSort() }) }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Інформація", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Назва: Notes App (Лаба 8)")
                Text("Сховище: Room + DataStore")
            }
        }
    }
}



@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Пошук нотаток...", color = MaterialTheme.colorScheme.outline) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Пошук", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun FolderListItem(name: String, count: Int, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "📁", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(end = 6.dp))
        Text(text = "$name [$count]", color = textColor, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun FolderGridItem(name: String, count: Int, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "📁", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = name, color = textColor, style = MaterialTheme.typography.titleMedium)
        Text(text = "$count нотаток", color = textColor.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun NoteEditorOverlay(noteId: Int, onExit: () -> Unit, windowSizeClass: WindowSizeClass? = null) {
    val viewModel: NoteDetailsViewModel = viewModel(key = "note_details_$noteId", factory = NoteDetailsViewModel.Factory(noteId))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state) { if (state is NoteDetailsState.Saved) onExit() }

    when (val s = state) {
        is NoteDetailsState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        is NoteDetailsState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(s.message, color = MaterialTheme.colorScheme.error) }
        is NoteDetailsState.Editing -> {
            NoteEditorContent(
                formState = s.formState,
                viewModel = viewModel,
                onExit = onExit,
                windowSizeClass = windowSizeClass
            )
        }
        is NoteDetailsState.Saved -> {}
    }
}

@Composable
fun NoteEditorContent(formState: NoteFormState, viewModel: NoteDetailsViewModel, onExit: () -> Unit, windowSizeClass: WindowSizeClass?) {
    val focusManager = LocalFocusManager.current
    val isTablet = windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var currentPhotoPath by remember { mutableStateOf<String?>(null) }
    var hasCameraPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var showCameraDenied by remember { mutableStateOf(false) }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && currentPhotoPath != null) {
            viewModel.updateState { it.copy(imagePath = currentPhotoPath) }
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasCameraPermission = isGranted
        showCameraDenied = !isGranted
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    var showLocationDenied by remember { mutableStateOf(false) }
    var locationLoading by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var locationAccuracy by remember { mutableStateOf<Float?>(null) }
    var locationTime by remember { mutableStateOf<Long?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        showLocationDenied = !granted
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = if (isTablet) Alignment.CenterHorizontally else Alignment.Start
        ) {
            Column(
                modifier = if (isTablet) Modifier.widthIn(max = 600.dp) else Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onExit,
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).size(40.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        IconButton(
                            onClick = { viewModel.saveNote() },
                            enabled = formState.isValid,
                            modifier = Modifier.background(
                                if (formState.isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, CircleShape
                            ).size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = "Зберегти",
                                tint = if (formState.isValid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Text(
                        text = if (formState.title.isEmpty()) "Створення нотатки" else "Редагування",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text("Основна інформація", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = formState.title,
                            onValueChange = { viewModel.updateState { s -> s.copy(title = it) } },
                            modifier = Modifier.fillMaxWidth().onFocusChanged { if (!it.isFocused) viewModel.validateTitle() },
                            label = { Text("Назва нотатки *") },
                            isError = formState.titleError != null,
                            supportingText = { formState.titleError?.let { Text(it) } },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = formState.content,
                            onValueChange = { viewModel.updateState { s -> s.copy(content = it) } },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                            label = { Text("Контент нотатки") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                    }
                }


                var isExpanded by remember { mutableStateOf(false) }

                val headerBgColor by animateColorAsState(
                    targetValue = if (isExpanded) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                )

                val rotation by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(headerBgColor)
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Додаткові параметри", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Розгорнути",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.graphicsLayer(rotationZ = rotation)
                    )
                }

                Card(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                    if (isExpanded) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = formState.sourceUrl,
                                onValueChange = { viewModel.updateState { s -> s.copy(sourceUrl = it) } },
                                modifier = Modifier.fillMaxWidth().onFocusChanged { if (!it.isFocused) viewModel.validateSourceUrl() },
                                label = { Text("URL Джерела *") },
                                placeholder = { Text("https://...") },
                                isError = formState.sourceUrlError != null,
                                supportingText = { formState.sourceUrlError?.let { Text(it) } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = formState.estimatedHours,
                                onValueChange = { viewModel.updateState { s -> s.copy(estimatedHours = it) } },
                                modifier = Modifier.fillMaxWidth().onFocusChanged { if (!it.isFocused) viewModel.validateEstimatedHours() },
                                label = { Text("Оцінка часу (години) *") },
                                isError = formState.estimatedHoursError != null,
                                supportingText = { formState.estimatedHoursError?.let { Text(it) } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                singleLine = true
                            )
                            var isDropdownExpanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedTextField(
                                    label = { Text("Вибрана папка *") },
                                    value = formState.folder?.displayName ?: "Оберіть папку *",
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxWidth().onFocusChanged { if (!it.isFocused) viewModel.validateFolder() }.clickable { isDropdownExpanded = true },
                                    readOnly = true,
                                    enabled = false,
                                    isError = formState.folderError != null,
                                    supportingText = { formState.folderError?.let { Text(it) } },
                                    colors = TextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledContainerColor = Color.Transparent)
                                )
                                DropdownMenu(expanded = isDropdownExpanded, onDismissRequest = { isDropdownExpanded = false }) {
                                    Folder.entries.forEach { folder ->
                                        DropdownMenuItem(text = { Text(folder.displayName) }, onClick = {
                                            viewModel.updateState { s -> s.copy(folder = folder, folderError = null) }
                                            isDropdownExpanded = false
                                        })
                                    }
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Додати в обране")
                                Switch(
                                    checked = formState.isFavorite,
                                    onCheckedChange = { isFav -> viewModel.updateState { it.copy(isFavorite = isFav) } }
                                )
                            }
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Пріоритет: ${formState.priority.roundToInt()}")
                                    Text("(1-10)", color = MaterialTheme.colorScheme.outline)
                                }
                                Slider(
                                    value = formState.priority,
                                    onValueChange = { prio -> viewModel.updateState { it.copy(priority = prio) } },
                                    valueRange = 1f..10f,
                                    steps = 8
                                )
                            }
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Фотографії", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                        if (hasCameraPermission) {
                            Button(onClick = {
                                val file = DeviceUtils.createImageFile(context)
                                currentPhotoPath = file.absolutePath
                                currentPhotoUri = DeviceUtils.getUriForFile(context, file)
                                takePictureLauncher.launch(currentPhotoUri!!)
                            }, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (formState.imagePath == null) "Зробити фото" else "Перезняти фото")
                            }

                            // Асинхронний показ фотографії через бібліотеку Coil
                            formState.imagePath?.let { path ->
                                val file = File(path)
                                if (file.exists()) {
                                    AsyncImage(
                                        model = file,
                                        contentDescription = "Фото нотатки",
                                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                            }
                        } else if (showCameraDenied) {
                            Text("Доступ до камери відхилено. Ви не можете додавати фото.", color = MaterialTheme.colorScheme.error)
                            Button(onClick = { DeviceUtils.openAppSettings(context) }, modifier = Modifier.fillMaxWidth()) {
                                Text("Перейти в налаштування")
                            }
                        } else {
                            Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }, modifier = Modifier.fillMaxWidth()) {
                                Text("Надати дозвіл на Камеру")
                            }
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Геолокація", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        if (hasLocationPermission) {
                            Button(onClick = {
                                    locationLoading = true
                                    locationError = null
                                    coroutineScope.launch {
                                        val loc = DeviceUtils.getCurrentLocation(context)
                                        if (loc != null) {
                                            viewModel.updateState { it.copy(latitude = loc.latitude, longitude = loc.longitude) }
                                            locationAccuracy = loc.accuracy
                                            locationTime = loc.time
                                        } else {
                                            locationError = "Не вдалося отримати локацію (перевірте, чи увімкнено GPS)"
                                        }
                                        locationLoading = false
                                    }
                                },
                                enabled = !locationLoading,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (locationLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.LocationOn, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Оновити локацію")
                                }
                            }

                            if (locationError != null) {
                                Text(locationError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }

                            if (formState.latitude != null && formState.longitude != null) {
                                val distance = DeviceUtils.calculateDistanceToCHNU(formState.latitude!!, formState.longitude!!)
                                val timeString = locationTime?.let { java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale("uk", "UA")).format(java.util.Date(it)) } ?: "Невідомо"

                                Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(12.dp)) {
                                    Text("Широта: ${formState.latitude}", style = MaterialTheme.typography.bodyMedium)
                                    Text("Довгота: ${formState.longitude}", style = MaterialTheme.typography.bodyMedium)
                                    Text("Точність: ±${locationAccuracy ?: "?"} метрів", style = MaterialTheme.typography.bodyMedium)
                                    Text("Час оновлення: $timeString", style = MaterialTheme.typography.bodyMedium)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    Text("Відстань до ЧНУ ім. Ю. Федьковича:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                                    Text(String.format("%.2f км", distance / 1000f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (showLocationDenied) {
                            Text("Доступ до геолокації відхилено.", color = MaterialTheme.colorScheme.error)
                            Button(onClick = { DeviceUtils.openAppSettings(context) }, modifier = Modifier.fillMaxWidth()) {
                                Text("Перейти в налаштування")
                            }
                        } else {
                            Button(onClick = {
                                locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text("Надати дозвіл на GPS")
                            }
                        }
                    }
                }

            }
        }
    }
}