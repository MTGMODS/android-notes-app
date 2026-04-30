package com.mtg.notes

import com.mtg.notes.network.NoteApiService
import com.mtg.notes.network.toLocalNote
import com.mtg.notes.network.toNetworkNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotesRepository(
    private val noteDao: NoteDao,
    private val apiService: NoteApiService
) {
    private val _isOffline = MutableStateFlow(false)
    val isOffline = _isOffline.asStateFlow()

    fun getAllNotesFlow(): Flow<List<Note>> = noteDao.getAllNotesFlow()

    suspend fun refreshNotes(): Result<Unit> {
        return try {
            val networkNotes = apiService.getAllNotes()
            val localNotes = networkNotes.map { it.toLocalNote() }

            noteDao.clearAll()
            localNotes.forEach { noteDao.insertNote(it) }

            _isOffline.value = false
            Result.success(Unit)
        } catch (e: Exception) {
            _isOffline.value = true
            Result.failure(e)
        }
    }

    suspend fun getNoteById(id: Int): Note? {
        return try {
            val networkNote = apiService.getNoteById(id.toString())
            val note = networkNote.toLocalNote()
            noteDao.updateNote(note)
            note
        } catch (e: Exception) {
            noteDao.getNoteById(id)
        }
    }


    suspend fun addNote(note: Note): Result<Note> {
        return try {
            val created = apiService.createNote(note.toNetworkNote())
            val localNote = created.toLocalNote()
            noteDao.insertNote(localNote)
            Result.success(localNote)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(note: Note): Result<Unit> {
        return try {
            apiService.deleteNote(note.id.toString())
            noteDao.deleteNote(note)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }
}

lateinit var globalNotesRepository: NotesRepository