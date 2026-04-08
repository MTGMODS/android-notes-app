package com.mtg.notes


import androidx.activity.enableEdgeToEdge
import android.os.Bundle
import android.text.format.DateUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import com.mtg.notes.ui.theme.NotesTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        runDemoNote()

        setContent {
            NotesTheme {
                NotesAppScreen()
            }
        }

    }
}

@Composable
fun NotesAppScreen() {

    var selectedFolder by remember { mutableStateOf<Folder?>(null) }
    var currentSortOption by remember { mutableStateOf(NotesStorage.sortOption) }

    var isGridView by remember { mutableStateOf(true) }
    var showFolders by remember { mutableStateOf(true) }

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
                Text(
                    text = "Нотатки",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium
                )
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
                    IconButton(onClick = {
                        currentSortOption = if (currentSortOption == SortOption.BY_CREATED_DATE) SortOption.BY_UPDATED_DATE else SortOption.BY_CREATED_DATE
                        NotesStorage.sortOption = currentSortOption
                    }) {
                        Icon(Icons.Default.Sort, contentDescription = "Сортування", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }

            Box(modifier = Modifier.padding(horizontal = 16.dp)) { SearchBar() }

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
                            FolderGridItem(name = "Всі", count = NotesStorage.getActiveNotes().size, isSelected = selectedFolder == null) { selectedFolder = null }
                        } else {
                            FolderListItem(name = "Всі", count = NotesStorage.getActiveNotes().size, isSelected = selectedFolder == null) { selectedFolder = null }
                        }
                    }
                    items(activeFolders) { folder ->
                        if (isGridView) {
                            FolderGridItem(name = folder.displayName, count = folderCounts[folder] ?: 0, isSelected = selectedFolder == folder) { selectedFolder = folder }
                        } else {
                            FolderListItem(name = folder.displayName, count = folderCounts[folder] ?: 0, isSelected = selectedFolder == folder) { selectedFolder = folder }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            val notesToShow = NotesStorage.getNotesFiltered(selectedFolder)

            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notesToShow) { note ->
                        NoteGridItem(note)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notesToShow) { note ->
                        NoteListItem(note)
                    }
                }
            }

        }

        FloatingActionButton(
            onClick = {  },
            containerColor = Color(0xFFFFB300),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Додати", tint = Color.Black)
        }
    }
}

@Composable
fun SearchBar() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Пошук",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Пошук нотаток",
            color = Color.Gray,
            fontSize = 16.sp
        )
    }
}


// UI для списку LazyRow
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

// UI для сітки
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



// UI для сітки LazyVerticalGrid
@Composable
fun NoteGridItem(note: Note) {
    val dateToDisplay = note.updatedAt ?: note.createdAt
    val dateString = if (DateUtils.isToday(dateToDisplay)) {
        SimpleDateFormat("HH:mm", Locale("uk", "UA")).format(Date(dateToDisplay))
    } else {
        SimpleDateFormat("dd MMM", Locale("uk", "UA")).format(Date(dateToDisplay))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Text(
            text = note.title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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

// UI для списку
@Composable
fun NoteListItem(note: Note) {
    val dateToDisplay = note.updatedAt ?: note.createdAt
    val dateString = if (DateUtils.isToday(dateToDisplay)) {
        SimpleDateFormat("HH:mm", Locale("uk", "UA")).format(Date(dateToDisplay))
    } else {
        SimpleDateFormat("dd MMM", Locale("uk", "UA")).format(Date(dateToDisplay))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
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
    }
}