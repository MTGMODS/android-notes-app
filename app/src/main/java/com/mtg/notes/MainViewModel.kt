package com.mtg.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val repository = globalNotesRepository

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFolder = MutableStateFlow<Folder?>(null)
    val selectedFolder: StateFlow<Folder?> = _selectedFolder.asStateFlow()

    private val _isSortAscending = MutableStateFlow(true)
    val isSortAscending: StateFlow<Boolean> = _isSortAscending.asStateFlow()

    val notesToShow: StateFlow<List<Note>> = combine(
        repository.getAllNotesFlow(), _searchQuery, _selectedFolder, _isSortAscending
    ) { allNotes, query, folder, sortAsc ->
        var filtered = allNotes

        if (folder != null) filtered = filtered.filter { it.folder == folder }
        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
            }
        }
        if (sortAsc) filtered.sortedBy { it.updatedAt } else filtered.sortedByDescending { it.updatedAt }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeFolders: StateFlow<Set<Folder>> = repository.getAllNotesFlow()
        .map { notes -> notes.mapNotNull { it.folder }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val folderCounts: StateFlow<Map<Folder, Int>> = repository.getAllNotesFlow()
        .map { notes -> notes.mapNotNull { it.folder }.groupingBy { it }.eachCount() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val totalNotesCount: StateFlow<Int> = repository.getAllNotesFlow()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(1000)
            _isLoading.value = false
        }
    }

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun selectFolder(folder: Folder?) { _selectedFolder.value = folder }
    fun toggleSortOrder() { _isSortAscending.value = !_isSortAscending.value }

    fun createNote(onCreated: (Int) -> Unit) {
        viewModelScope.launch {
            val newNote = Note(title = "Нова нотатка", content = "", folder = _selectedFolder.value)
            val id = repository.addNote(newNote).toInt()
            onCreated(id)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { repository.deleteNote(note) }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch {
            val updatedNote = note.copy(isFavorite = !note.isFavorite)
            repository.updateNote(updatedNote)
        }
    }
}