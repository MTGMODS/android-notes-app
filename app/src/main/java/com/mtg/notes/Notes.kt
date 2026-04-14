package com.mtg.notes

import androidx.compose.runtime.mutableStateListOf

enum class SortOption { BY_CREATED_DATE, BY_UPDATED_DATE }

enum class Folder(val displayName: String) {
    STUDY("Навчання"),
    WORK("Робота"),
    PERSONAL("Особисте"),
    IDEAS("Ідеї")
}

open class Note(
    val id: Int = generateNextId(),
    var title: String = "Без назви",
    var content: String,
    var folder: Folder? = null
) {
    var updatedAt: Long = System.currentTimeMillis()

    constructor(content: String, folder: Folder? = null) : this(
        title = content.lineSequence().firstOrNull { it.isNotBlank() } ?: "Без назви",
        content = content,
        folder = folder
    )

    companion object {
        private var currentIdCounter = 1
        fun generateNextId(): Int {
            return currentIdCounter++
        }
    }

    open fun edit(newContent: String) {
        this.content = newContent
        this.updatedAt = System.currentTimeMillis()
    }

    open fun edit(newTitle: String, newContent: String, newFolder: Folder?) {
        this.title = newTitle
        this.content = newContent
        this.folder = newFolder
        this.updatedAt = System.currentTimeMillis()
    }

    open fun getPreviewText(): String {
        return if (content.length > 25) content.substring(0, 25) + "..." else content
    }

}

object NotesStorage {
    private val activeNotes = mutableStateListOf<Note>()

    var sortOption: SortOption = SortOption.BY_CREATED_DATE

    fun addNote(note: Note) {
        activeNotes.add(note)
    }

    fun deleteNote(note: Note) {
        activeNotes.remove(note)
    }

    fun getActiveNotes(): List<Note> {
        return activeNotes
    }

    fun getActiveFolders(): Set<Folder> {
        return activeNotes.mapNotNull { it.folder }.toSet()
    }

    fun getFolderCounts(): Map<Folder, Int> {
        return activeNotes.mapNotNull { it.folder }.groupingBy { it }.eachCount()
    }

    fun getNotesFiltered(selectedFolder: Folder?): List<Note> {
        val filtered = if (selectedFolder == null) activeNotes else activeNotes.filter { it.folder == selectedFolder }
        return filtered.sortedByDescending { it.updatedAt }
    }

}