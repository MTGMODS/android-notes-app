package com.mtg.notes

import androidx.activity.enableEdgeToEdge
import android.os.Bundle
import android.text.format.DateUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider

import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.mtg.notes.ui.theme.NotesTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NotesTheme {
                NotesAppScreen()
            }
        }

    }
}

@Composable
fun NotesAppScreen() {

    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    var isFabExpanded by remember { mutableStateOf(false) }

    var showFolderDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var folderNameInput by remember { mutableStateOf("") }

    var selectedFolder by remember { mutableStateOf<Folder?>(null) }
    var isGridView by remember { mutableStateOf(true) }
    var showFolders by remember { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }

    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    if (editingNote != null) {
        BackHandler { editingNote = null }
        NoteEditorOverlay(
            note = editingNote!!,
            onExit = { editingNote = null }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(top = 48.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
//                            text = "Нотатки (" + NotesStorage.getActiveNotes().size + ")",
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
                    }

                    Row {
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
                        IconButton(onClick = { isGridView = !isGridView }) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Змінити вигляд",
                                tint = if (isGridView) Color.Gray else MaterialTheme.colorScheme.primary
                            )
                        }

                    }
                }

                if (NotesStorage.getActiveNotes().isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "📝", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Створіть першу нотатку",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val activeFolders = NotesStorage.getActiveFolders().toList()
                    val folderCounts = NotesStorage.getFolderCounts()

                    if (showFolders) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                if (isGridView) {
                                    FolderGridItem(
                                        name = "Всі",
                                        count = NotesStorage.getActiveNotes().size,
                                        isSelected = selectedFolder == null
                                    ) { selectedFolder = null }
                                } else {
                                    FolderListItem(
                                        name = "Всі",
                                        count = NotesStorage.getActiveNotes().size,
                                        isSelected = selectedFolder == null
                                    ) { selectedFolder = null }
                                }
                            }
                            items(activeFolders) { folder ->
                                if (isGridView) {
                                    FolderGridItem(
                                        name = folder.displayName,
                                        count = folderCounts[folder] ?: 0,
                                        isSelected = selectedFolder == folder
                                    ) { selectedFolder = folder }
                                } else {
                                    FolderListItem(
                                        name = folder.displayName,
                                        count = folderCounts[folder] ?: 0,
                                        isSelected = selectedFolder == folder
                                    ) { selectedFolder = folder }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    val notesToShow by remember(selectedFolder, searchQuery, NotesStorage.getActiveNotes().size) {
                        derivedStateOf {
                            NotesStorage.getNotesFiltered(selectedFolder).filter { note ->
                                searchQuery.isEmpty()
                                        || note.title.contains(searchQuery, ignoreCase = true)
                                        || note.content.contains(searchQuery, ignoreCase = true)
                            }
                        }
                    }

                    if (isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 160.dp),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 80.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(notesToShow) { note ->
                                NoteGridItem(
                                    note = note,
                                    onClick = { editingNote = note },
                                    onDelete = { noteToDelete = note }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 80.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(notesToShow) { note ->
                                NoteListItem(
                                    note = note,
                                    onClick = { editingNote = note },
                                    onDelete = { noteToDelete = note }
                                )
                            }
                        }
                    }
                }

            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
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
                            editingNote = newNote
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

        if (showFolderDialog) {
            AlertDialog(
                onDismissRequest = { showFolderDialog = false },
                title = { Text("Нова папка") },
                text = {
                    Column {
                        Text("Вкажіть назву папки")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = folderNameInput,
                            onValueChange = { folderNameInput = it },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        Toast.makeText(context, "Симуляція нової папки: $folderNameInput", Toast.LENGTH_SHORT).show()
                        folderNameInput = ""
                        showFolderDialog = false
                    }) { Text("Створити папку") }
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
                    TextButton(onClick = {
                        NotesStorage.deleteNote(noteToDelete!!)
                        noteToDelete = null
                    }) {
                        Text("Так", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { noteToDelete = null }) { Text("Ні") }
                }
            )
        }

    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Пошук нотаток...", color = Color.Gray) },
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
        Text(text = "📁", fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
        Text(text = "$name [$count]", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
        Text(text = "📁", fontSize = 32.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = name, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = "$count нотаток", color = textColor.copy(alpha = 0.7f), fontSize = 12.sp)
    }
}

@Composable
fun NoteGridItem(note: Note, onClick: () -> Unit, onDelete: () -> Unit) {
    val dateString = if (DateUtils.isToday(note.updatedAt)) {
        SimpleDateFormat("HH:mm", Locale("uk", "UA")).format(Date(note.updatedAt))
    } else {
        SimpleDateFormat("dd MMM", Locale("uk", "UA")).format(Date(note.updatedAt))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = note.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Видалити",
                    tint = Color.Red.copy(alpha = 0.6f)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = note.getPreviewText(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f) // Виштовхує низ
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = dateString, color = Color.Gray, fontSize = 12.sp)
            Text(text = note.folder?.displayName ?: "", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NoteListItem(note: Note, onClick: () -> Unit, onDelete: () -> Unit) {
    val dateString = if (DateUtils.isToday(note.updatedAt)) {
        SimpleDateFormat("HH:mm", Locale("uk", "UA")).format(Date(note.updatedAt))
    } else {
        SimpleDateFormat("dd MMM", Locale("uk", "UA")).format(Date(note.updatedAt))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = note.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.getPreviewText(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(text = dateString, color = Color.Gray, fontSize = 12.sp)
            Text(text = note.folder?.displayName ?: "", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Видалити", tint = Color.Red.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun NoteEditorOverlay(note: Note, onExit: () -> Unit) {
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }
    var currentFolder by remember { mutableStateOf(note.folder) }

    var isDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(title, content, currentFolder) {
        note.edit(title, content, currentFolder)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onExit,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                OutlinedButton(
                    onClick = { isDropdownExpanded = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "📁 " + (currentFolder?.displayName ?: "Без папки"),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Без папки") },
                        onClick = { currentFolder = null; isDropdownExpanded = false }
                    )
                    Folder.entries.forEach { folder ->
                        DropdownMenuItem(
                            text = { Text(folder.displayName) },
                            onClick = { currentFolder = folder; isDropdownExpanded = false }
                        )
                    }
                }
            }

            Text(
                text = "${content.length} симв.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            placeholder = { Text("Назва нотатки...") },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("Почніть писати тут...") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}