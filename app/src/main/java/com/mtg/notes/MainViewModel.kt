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

    private val _allNotes = MutableStateFlow(repository.getAllNotes())

    val notesToShow: StateFlow<List<Note>> = combine(
        _allNotes, _searchQuery, _selectedFolder, _isSortAscending
    ) { notes, query, folder, sortAsc ->
        var filtered = notes

        if (folder != null) filtered = filtered.filter { it.folder == folder }
        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
            }
        }

        if (sortAsc) filtered.sortedBy { it.title } else filtered.sortedByDescending { it.title }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeFolders: StateFlow<Set<Folder>> = _allNotes
        .map { notes -> notes.mapNotNull { it.folder }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val folderCounts: StateFlow<Map<Folder, Int>> = _allNotes
        .map { notes -> notes.mapNotNull { it.folder }.groupingBy { it }.eachCount() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(2000)
            refreshNotes()
            _isLoading.value = false
        }
    }

    fun refreshNotes() {
        _allNotes.value = repository.getAllNotes()
    }

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun selectFolder(folder: Folder?) { _selectedFolder.value = folder }
    fun toggleSortOrder() { _isSortAscending.value = !_isSortAscending.value }

    fun deleteNote(note: Note) {
        repository.deleteNote(note)
        refreshNotes()
    }

    fun createNote(): Int {
        val newNote = Note("")
        repository.addNote(newNote)
        refreshNotes()
        return newNote.id
    }
}