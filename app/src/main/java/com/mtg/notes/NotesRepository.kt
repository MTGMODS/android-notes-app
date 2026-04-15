package com.mtg.notes

class NotesRepository {
    private val activeNotes = mutableListOf<Note>()

    init {
        activeNotes.add(Note(id = 1, title = "Ідея для додатку", content = "Зробити MVVM архітектуру", folder = Folder.WORK))
        activeNotes.add(Note(id = 2, title = "Купити в магазині", content = "Молоко, хліб, яйця", folder = Folder.PERSONAL))
    }

    fun getActiveNotes(): List<Note> {
        return activeNotes.toList()
    }

    fun getNoteById(id: Int): Note? {
        return activeNotes.find { it.id == id }
    }

    fun addNote(note: Note) {
        activeNotes.add(note)
    }

    fun deleteNote(note: Note) {
        activeNotes.remove(note)
    }

    fun updateNote(note: Note) {
        val index = activeNotes.indexOfFirst { it.id == note.id }
        if (index != -1) {
            activeNotes[index] = note
        }
    }

    fun getActiveFolders(): Set<Folder> {
        return activeNotes.mapNotNull { it.folder }.toSet()
    }

    fun getFolderCounts(): Map<Folder, Int> {
        return activeNotes.mapNotNull { it.folder }.groupingBy { it }.eachCount()
    }

    fun getNotesFiltered(selectedFolder: Folder?): List<Note> {
        val filtered =
            if (selectedFolder == null) activeNotes else activeNotes.filter { it.folder == selectedFolder }
        return filtered.sortedByDescending { it.updatedAt }
    }

}

val globalNotesRepository = NotesRepository()
