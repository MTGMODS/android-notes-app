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
    val createdAt: Long = System.currentTimeMillis() - (0..10000000).random() // Трохи рандому для демонстрації

    var updatedAt: Long? = null  // Nullable

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

    open fun edit(newTitle: String, newContent: String) {
        this.title = newTitle
        this.content = newContent
        this.updatedAt = System.currentTimeMillis()
    }

    open fun getPreviewText(): String {
        return if (content.length > 25) content.substring(0, 25) + "..." else content
    }

    fun clearContent() {
        this.content = ""
    }
}

class TrashedNote(id: Int, title: String, content: String, val deletedAt: Long = System.currentTimeMillis()) : Note(id = id, title = title, content = content) {
    override fun getPreviewText(): String {
        return "🗑 [КОШИК] " + super.getPreviewText()
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
        return when (sortOption) {
            SortOption.BY_CREATED_DATE -> activeNotes.sortedByDescending { it.createdAt }
            SortOption.BY_UPDATED_DATE -> activeNotes.sortedByDescending { it.updatedAt ?: it.createdAt }
        }
    }

    fun getActiveFolders(): Set<Folder> {
        return activeNotes.mapNotNull { it.folder }.toSet()
    }

    fun getFolderCounts(): Map<Folder, Int> {
        return activeNotes.mapNotNull { it.folder }.groupingBy { it }.eachCount()
    }

    fun getNotesFiltered(selectedFolder: Folder?): List<Note> {
        val filtered = if (selectedFolder == null) activeNotes else activeNotes.filter { it.folder == selectedFolder }
        return when (sortOption) {
            SortOption.BY_CREATED_DATE -> filtered.sortedByDescending { it.createdAt }
            SortOption.BY_UPDATED_DATE -> filtered.sortedByDescending { it.updatedAt ?: it.createdAt }
        }
    }

}


fun String.charCountInfo(): String = "Символів введено: ${this.length}"


fun Note.printForUi() {
    val editStatus = this.updatedAt?.let { "Змінено ${this.updatedAt}" } ?: "Створено ${this.createdAt}"
    println("ID:${this.id} | ${this.title} | ${this.getPreviewText()} | $editStatus")
}

fun runDemoNote() {
    val uniNote = Note(title = "Розклад універа", content = "Понеділок: Лаби мікросервіси Нікітін 8-103", folder = Folder.STUDY)
    val autoSchoolNote = Note(title = "АШ", content = "Пасивна безпека: ремінь, підголівкник", folder = Folder.PERSONAL)
    val fastNote = Note("Купити чай по дорозі")

    NotesStorage.addNote(uniNote)
    NotesStorage.addNote(autoSchoolNote)
    NotesStorage.addNote(fastNote)
}