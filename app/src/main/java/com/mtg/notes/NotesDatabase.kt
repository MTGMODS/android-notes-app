package com.mtg.notes

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Note::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NotesDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    private class NotesDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    val dao = database.noteDao()
                    dao.insertNote(Note(title = "Ідея для додатку", content = "Зробити MVVM архітектуру з Room", folder = Folder.WORK))
                    dao.insertNote(Note(title = "Список покупок", content = "Молоко, хліб, банани", folder = Folder.PERSONAL))
                    dao.insertNote(Note(title = "Книги на літо", content = "Гаррі Поттер, Дюна, Відьмак", folder = Folder.IDEAS, isFavorite = true))
                    dao.insertNote(Note(title = "Університет", content = "Закрити семестр без перездач", folder = Folder.STUDY))
                    dao.insertNote(Note(title = "Тренування", content = "Записатися в спортзал", folder = Folder.PERSONAL, isFavorite = true))
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: NotesDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): NotesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                    "notes_database"
                ).addCallback(NotesDatabaseCallback(scope)).build()
                INSTANCE = instance
                instance
            }
        }
    }
}