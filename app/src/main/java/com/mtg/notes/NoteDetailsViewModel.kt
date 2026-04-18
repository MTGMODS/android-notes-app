package com.mtg.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface NoteDetailsState {
    object Loading : NoteDetailsState
    data class Success(val note: Note) : NoteDetailsState
    data class Error(val message: String) : NoteDetailsState
}

class NoteDetailsViewModel(private val noteId: Int) : ViewModel() {
    private val repository = globalNotesRepository

    private val _uiState = MutableStateFlow<NoteDetailsState>(NoteDetailsState.Loading)
    val uiState: StateFlow<NoteDetailsState> = _uiState.asStateFlow()

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            _uiState.value = NoteDetailsState.Loading
            delay(500) // Імітація завантаження

            val note = repository.getNoteById(noteId)
            if (note != null) {
                _uiState.value = NoteDetailsState.Success(note)
            } else {
                _uiState.value = NoteDetailsState.Error("Нотатку не знайдено")
            }
        }
    }

    fun updateNote(title: String, content: String, folder: Folder?) {
        val currentState = _uiState.value
        if (currentState is NoteDetailsState.Success) {
            val updatedNote = currentState.note
            updatedNote.edit(title, content, folder)
            repository.updateNote(updatedNote)
        }
    }

    class Factory(private val noteId: Int) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteDetailsViewModel(noteId) as T
        }
    }
}