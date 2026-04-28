package com.mtg.notes

import kotlinx.coroutines.flow.Flow

class NotesRepository(private val noteDao: NoteDao) {

    fun getAllNotesFlow(): Flow<List<Note>> {
        return noteDao.getAllNotesFlow()
    }

    suspend fun getNoteById(id: Int): Note? {
        return noteDao.getNoteById(id)
    }

    suspend fun addNote(note: Note): Long {
        return noteDao.insertNote(note)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }
}

lateinit var globalNotesRepository: NotesRepository