package com.mtg.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val repository = globalNotesRepository
    private val settings = globalSettingsRepository

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val isOffline: StateFlow<Boolean> = repository.isOffline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFolder = MutableStateFlow<Folder?>(null)
    val selectedFolder: StateFlow<Folder?> = _selectedFolder.asStateFlow()

    val isSortAscending: StateFlow<Boolean> = settings.isSortAscendingFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val showFavoritesOnly: StateFlow<Boolean> = settings.showFavoritesOnlyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val notesToShow: StateFlow<List<Note>> = combine(
        repository.getAllNotesFlow(),
        _searchQuery,
        _selectedFolder,
        settings.isSortAscendingFlow,
        settings.showFavoritesOnlyFlow
    ) { allNotes, query, folder, sortAsc, favoritesOnly ->
        var filtered = allNotes
        if (favoritesOnly) filtered = filtered.filter { it.isFavorite }
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

    init { refreshData() }


    fun refreshData(isSwipe: Boolean = false) {
        viewModelScope.launch {
            if (isSwipe) {
                _isRefreshing.value = true
                delay(1000)
            } else {
                _isLoading.value = true
            }

            val result = repository.refreshNotes()
            if (result.isFailure) {
                _errorMessage.emit("Немає зв'язку з сервером. Показано офлайн-кеш.")
            }

            _isRefreshing.value = false
            _isLoading.value = false
        }
    }

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun selectFolder(folder: Folder?) { _selectedFolder.value = folder }
    fun toggleSortOrder() { viewModelScope.launch { settings.toggleSortOrder() } }
    fun toggleFavoritesOnly() { viewModelScope.launch { settings.toggleFavoritesOnly() } }

    fun createNote(title: String, content: String, folder: Folder?, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val newNote = Note(title = title, content = content, folder = folder)
            val result = repository.addNote(newNote)
            if (result.isFailure) {
                _errorMessage.emit("Помилка створення нотатки на сервері")
            }
            _isLoading.value = false
            onComplete()
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            // _isLoading.value = true
            val result = repository.deleteNote(note)
            if (result.isFailure) {
                _errorMessage.emit("Не вдалося видалити нотатку з сервера")
            }
            // _isLoading.value = false
        }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch {
            val updatedNote = note.copy(isFavorite = !note.isFavorite)
            repository.updateNote(updatedNote)
        }
    }
}