package com.mtg.notes

enum class SortOption { BY_CREATED_DATE, BY_UPDATED_DATE }

enum class Folder(val displayName: String) {
    GENERAL("Загальні"),
    STUDY("Навчання"),
    WORK("Робота"),
    PERSONAL("Особисте"),
    IDEAS("Ідеї")
}

open class Note(
    val id: Int = generateNextId(),
    var title: String = "Без назви",
    var content: String,
    val folder: Folder = Folder.GENERAL
) {
    val createdAt: Long = System.currentTimeMillis()

    var updatedAt: Long? = null  // Nullable

    constructor(content: String) : this(title = "Без назви", content = content, folder = Folder.GENERAL)

    companion object { // static метод
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
    private val activeNotes = mutableListOf<Note>()
    private val trashBin = mutableListOf<TrashedNote>()

    var sortOption: SortOption = SortOption.BY_CREATED_DATE

    fun addNote(note: Note) {
        activeNotes.add(note)
    }

    fun moveToTrash(noteId: Int) {
        val noteToTrash = activeNotes.find { it.id == noteId } // lambda func

        // Розпакування Nullable за допомогою let
        noteToTrash?.let { note ->
            activeNotes.remove(note)
            val trashed = TrashedNote(note.id, note.title, note.content)
            trashBin.add(trashed)
            println("-> Нотатку '${note.title}' переміщено в кошик.")
        } ?: println("-> Помилка: Нотатку з ID $noteId не знайдено.")
    }

    fun getActiveNotes(): List<Note> {  // lambda для сортування
        return when (sortOption) {
            SortOption.BY_CREATED_DATE -> activeNotes.sortedByDescending { it.createdAt }
            SortOption.BY_UPDATED_DATE -> activeNotes.sortedByDescending { it.updatedAt ?: it.createdAt }
        }
    }

    // 1. Set (Унікальні папки)
    fun getActiveFolders(): Set<Folder> {
        return activeNotes.map { it.folder }.toSet()
    }

    // 2. Map (Кількість нотаток у папці)
    fun getFolderCounts(): Map<Folder, Int> {
        return activeNotes.groupBy { it.folder }.mapValues { it.value.size }
    }

    // 3. Фільтрація + Сортування
    fun getNotesFiltered(selectedFolder: Folder?): List<Note> {
        val filtered = if (selectedFolder == null) activeNotes else activeNotes.filter { it.folder == selectedFolder }

        return when (sortOption) {
            SortOption.BY_CREATED_DATE -> filtered.sortedByDescending { it.createdAt }
            SortOption.BY_UPDATED_DATE -> filtered.sortedByDescending { it.updatedAt ?: it.createdAt }
        }
    }

    fun getTrashedNotes(): List<TrashedNote> = trashBin
}


fun String.charCountInfo(): String = "Символів введено: ${this.length}"


fun Note.printForUi() {
    val editStatus = this.updatedAt?.let { "Змінено ${this.updatedAt}" } ?: "Створено ${this.createdAt}"
    println("ID:${this.id} | ${this.title} | ${this.getPreviewText()} | $editStatus")
}

fun TrashedNote.getDaysUntilPermanentDeletion(): Int { return 30 } //

fun runDemoNote() {
    println("\nЗАПУСК ДОДАТКУ 'НОТАТКИ'\n")

    val uniNote = Note(title = "Розклад універа", content = "Понеділок: Лаби мікросервіси Нікітін 8-103", folder = Folder.STUDY)
    val autoSchoolNote = Note(title = "АШ", content = "Пасивна безпека: ремінь, підголівкник", folder = Folder.PERSONAL)
    val fastNote = Note("Купити чай по дорозі")

    uniNote.printForUi()
    autoSchoolNote.printForUi()
    fastNote.printForUi()

    NotesStorage.addNote(uniNote)
    NotesStorage.addNote(autoSchoolNote)
    NotesStorage.addNote(fastNote)

    println("\nРедагування нотатки")
    autoSchoolNote.edit("Теорія для АШ", "Пасивна безпека. Активна - подушка")
    autoSchoolNote.printForUi()

    uniNote.edit("Понеділок: Вільний (лаби з мікросервісів 50+)")
    uniNote.printForUi()

    println("\nВидалення нотатки")
    NotesStorage.moveToTrash(fastNote.id)

    println("\nАКТИВНІ НОТАТКИ (Сортування за датою оновлення)")
    NotesStorage.sortOption = SortOption.BY_UPDATED_DATE
    NotesStorage.getActiveNotes().forEach { it.printForUi() }

    println("\n--- КОРЗИНА ---")
    NotesStorage.getTrashedNotes().forEach { trashed ->
        trashed.printForUi()
        println("   До повного видалення: ${trashed.getDaysUntilPermanentDeletion()} днів")
    }

    println("\nСимуляція ніби редагую нотатку про аш")
    println(autoSchoolNote.content.charCountInfo())
}