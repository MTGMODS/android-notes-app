package com.mtg.notes

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NoteFormState(
    val title: String = "",
    val content: String = "",
    val folder: Folder? = null,
    val isFavorite: Boolean = false,
    val sourceUrl: String = "",
    val estimatedHours: String = "",
    val priority: Float = 1f,

    val imagePath: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,

    val titleError: String? = null,
    val contentError: String? = null,
    val folderError: String? = null,
    val sourceUrlError: String? = null,
    val estimatedHoursError: String? = null
) {
    val isValid: Boolean get() = title.isNotBlank() && content.isNotBlank() &&
            folder != null && titleError == null && contentError == null &&
            sourceUrlError == null && estimatedHoursError == null
}
sealed interface NoteDetailsState {
    object Loading : NoteDetailsState
    data class Error(val message: String) : NoteDetailsState
    data class Editing(val formState: NoteFormState, val originalNote: Note?) : NoteDetailsState
    object Saved : NoteDetailsState
}

class NoteDetailsViewModel(private val noteId: Int) : ViewModel() {
    private val repository = globalNotesRepository

    private val _uiState = MutableStateFlow<NoteDetailsState>(NoteDetailsState.Loading)
    val uiState: StateFlow<NoteDetailsState> = _uiState.asStateFlow()

    private var originalNote: Note? = null

    init { loadNote() }

    private fun loadNote() {
        viewModelScope.launch {
            _uiState.value = NoteDetailsState.Loading
            if (noteId == -1) {
                _uiState.value = NoteDetailsState.Editing(NoteFormState(), null)
            } else {
                val note = repository.getNoteById(noteId)
                if (note != null) {
                    originalNote = note
                    val initialState = NoteFormState(
                        title = note.title,
                        content = note.content,
                        sourceUrl = note.sourceUrl,
                        estimatedHours = note.estimatedHours.toString(),
                        folder = note.folder,
                        isFavorite = note.isFavorite,
                        priority = note.priority.toFloat(),
                        imagePath = note.imagePath,
                        latitude = note.latitude,
                        longitude = note.longitude
                    )
                    _uiState.value = NoteDetailsState.Editing(initialState, note)
                } else {
                    _uiState.value = NoteDetailsState.Error("Нотатку не знайдено")
                }
            }
        }
    }

    fun updateState(update: (NoteFormState) -> NoteFormState) {
        val currentState = _uiState.value
        if (currentState is NoteDetailsState.Editing) {
            _uiState.value = currentState.copy(formState = update(currentState.formState))
        }
    }

    fun validateTitle() {
        updateState { state ->
            val error = if (state.title.isBlank()) "Поле не може бути порожнім"
            else if (state.title.length < 3) "Мінімум 3 символи" else null
            state.copy(titleError = error)
        }
    }

    fun validateSourceUrl() {
        updateState { state ->
            val isValidUrl = Patterns.WEB_URL.matcher(state.sourceUrl).matches()
            val error = if (state.sourceUrl.isBlank()) "Введіть посилання"
            else if (!isValidUrl) "Некоректний формат URL" else null
            state.copy(sourceUrlError = error)
        }
    }

    fun validateEstimatedHours() {
        updateState { state ->
            val hours = state.estimatedHours.toIntOrNull()
            val error = if (hours == null) "Введіть число"
            else if (hours <= 0) "Час має бути більшим за 0" else null
            state.copy(estimatedHoursError = error)
        }
    }

    fun validateFolder() {
        updateState { state ->
            val error = if (state.folder == null) "Оберіть папку" else null
            state.copy(folderError = error)
        }
    }

    fun validateAll() {
        validateTitle(); validateSourceUrl(); validateEstimatedHours(); validateFolder()
    }

    fun saveNote() {
        val currentState = _uiState.value
        if (currentState is NoteDetailsState.Editing) {
            validateAll()
            val finalState = (_uiState.value as NoteDetailsState.Editing).formState

            if (finalState.isValid) {
                viewModelScope.launch {
                    _uiState.value = NoteDetailsState.Loading

                    val hours = finalState.estimatedHours.toIntOrNull() ?: 0
                    if (noteId == -1) {
                        val newNote = Note(
                            title = finalState.title, content = finalState.content, folder = finalState.folder,
                            sourceUrl = finalState.sourceUrl, estimatedHours = hours, priority = finalState.priority.toInt(),
                            isFavorite = finalState.isFavorite,
                            imagePath = finalState.imagePath,
                            latitude = finalState.latitude,
                            longitude = finalState.longitude
                        )
                        repository.addNote(newNote)
                    } else {
                        val updatedNote = originalNote!!.copy(
                            title = finalState.title, content = finalState.content, folder = finalState.folder,
                            sourceUrl = finalState.sourceUrl, estimatedHours = hours, priority = finalState.priority.toInt(),
                            isFavorite = finalState.isFavorite, updatedAt = System.currentTimeMillis(),
                            imagePath = finalState.imagePath,
                            latitude = finalState.latitude,
                            longitude = finalState.longitude
                        )
                        repository.updateNote(updatedNote)
                    }
                    _uiState.value = NoteDetailsState.Saved
                }
            }
        }
    }

    class Factory(private val noteId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteDetailsViewModel(noteId) as T
        }
    }
}