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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp

enum class BottomTab(val title: String, val icon: ImageVector) {
    LIST("Список", Icons.Default.List),
    GRID("Плитка", Icons.Default.GridView),
    PROFILE("Профіль", Icons.Default.Person)
}

@Composable
fun MainTabScreen(userName: String, globalNavController: NavController) {
    var currentTab by remember { mutableStateOf(BottomTab.LIST) }
    var currentUserName by remember { mutableStateOf(userName) }

    // === СТАНИ З ЛАБИ 4 ===
    val context = LocalContext.current
    var selectedFolder by remember { mutableStateOf<Folder?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showFolders by remember { mutableStateOf(true) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var folderNameInput by remember { mutableStateOf("") }
    var isFabExpanded by remember { mutableStateOf(false) }

    val notesToShow by remember(selectedFolder, searchQuery, NotesStorage.getActiveNotes().size) {
        derivedStateOf {
            NotesStorage.getNotesFiltered(selectedFolder).filter { note ->
                searchQuery.isEmpty() ||
                        note.title.contains(searchQuery, ignoreCase = true) ||
                        note.content.contains(searchQuery, ignoreCase = true)
            }
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
            if (currentTab != BottomTab.PROFILE) {
                Column(horizontalAlignment = Alignment.End) {
                    if (isFabExpanded) {
                        SmallFloatingActionButton(
                            onClick = { isFabExpanded = false; showFolderDialog = true },
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
                            onClick = {
                                isFabExpanded = false
                                val newNote = Note("")
                                NotesStorage.addNote(newNote)
                                globalNavController.navigate(Screen.NoteDetails.createRoute(newNote.id))
                            },
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
                        onClick = { isFabExpanded = !isFabExpanded },
                        containerColor = Color(0xFFFFB300),
                        shape = CircleShape
                    ) {
                        Icon(if (isFabExpanded) Icons.Default.Close else Icons.Default.Add, "Меню", tint = Color.Black)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (currentTab) {
                BottomTab.LIST, BottomTab.GRID -> {
                    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Нотатки",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (NotesStorage.getActiveNotes().size > 5) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    color = Color.Red.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Ліміт Free версії",
                                        color = Color.Red,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            IconButton(onClick = {
                                showFolders = !showFolders
                                if (!showFolders) {
                                    selectedFolder = null
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = "Папки",
                                    tint = if (showFolders) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        }

                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        val activeFolders = NotesStorage.getActiveFolders().toList()
                        val folderCounts = NotesStorage.getFolderCounts()
                        if (showFolders) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    if (currentTab == BottomTab.LIST) {
                                        FolderListItem(name = "Всі", count = NotesStorage.getActiveNotes().size, isSelected = selectedFolder == null) { selectedFolder = null }
                                    } else if (currentTab == BottomTab.GRID) {
                                        FolderGridItem(name = "Всі", count = NotesStorage.getActiveNotes().size, isSelected = selectedFolder == null) { selectedFolder = null }
                                    }
                                }
                                items(activeFolders) { folder ->
                                    if (currentTab == BottomTab.LIST) {
                                        FolderListItem(name = folder.displayName, count = folderCounts[folder] ?: 0, isSelected = selectedFolder == folder) { selectedFolder = folder }
                                    } else if (currentTab == BottomTab.GRID) {
                                        FolderGridItem(name = folder.displayName, count = folderCounts[folder] ?: 0, isSelected = selectedFolder == folder) { selectedFolder = folder }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        if (NotesStorage.getActiveNotes().isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "📝", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Створіть першу нотатку", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 18.sp)
                                }
                            }
                        } else {
                            if (currentTab == BottomTab.LIST) {
                                LazyColumn(
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(notesToShow) { note ->
                                        NoteListItem(
                                            note = note,
                                            onClick = { globalNavController.navigate(Screen.NoteDetails.createRoute(note.id)) },
                                            onDelete = { noteToDelete = note }
                                        )
                                    }
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 160.dp),
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(notesToShow) { note ->
                                        NoteGridItem(
                                            note = note,
                                            onClick = { globalNavController.navigate(Screen.NoteDetails.createRoute(note.id)) },
                                            onDelete = { noteToDelete = note }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                BottomTab.PROFILE -> {
                    ProfileTab(userName = currentUserName, onNameChange = { currentUserName = it })
                }
            }
        }
    }

    if (showFolderDialog) {
        AlertDialog(
            onDismissRequest = { showFolderDialog = false },
            title = { Text("Нова папка") },
            text = {
                OutlinedTextField(value = folderNameInput, onValueChange = { folderNameInput = it }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = {
                    Toast.makeText(context, "Симуляція нової папки: $folderNameInput", Toast.LENGTH_SHORT).show()
                    folderNameInput = ""
                    showFolderDialog = false
                }) { Text("Створити") }
            },
            dismissButton = {
                TextButton(onClick = { showFolderDialog = false }) { Text("Відміна") }
            }
        )
    }

    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = "Увага", tint = Color.Red) },
            title = { Text("Увага!") },
            text = { Text("Ви дійсно хочете видалити нотатку \"${noteToDelete?.title}\"?") },
            confirmButton = {
                TextButton(onClick = { NotesStorage.deleteNote(noteToDelete!!); noteToDelete = null }) {
                    Text("Так", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) { Text("Ні") }
            }
        )
    }
}

@Composable
fun ProfileTab(userName: String, onNameChange: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Профіль користувача", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = onNameChange,
            label = { Text("Ваше ім'я") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(48.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Інформація про додаток", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Назва: Notes App (Лаба 5)")
                Text("Версія: 5.0.0")
                Text("Розробник: Marher Bohdan")
            }
        }
    }
}