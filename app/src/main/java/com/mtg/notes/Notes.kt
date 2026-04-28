package com.mtg.notes

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class Folder(val displayName: String) {
    STUDY("Навчання"),
    WORK("Робота"),
    PERSONAL("Особисте"),
    IDEAS("Ідеї")
}

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var title: String = "Без назви",
    var content: String,
    var folder: Folder? = null,
    var isFavorite: Boolean = false,
    var updatedAt: Long = System.currentTimeMillis()
) {
    fun getPreviewText(): String {
        return if (content.length > 25) content.substring(0, 25) + "..." else content
    }
}

class Converters {
    @TypeConverter
    fun fromFolder(folder: Folder?): String? {
        return folder?.name
    }

    @TypeConverter
    fun toFolder(name: String?): Folder? {
        return name?.let { enumValueOf<Folder>(it) }
    }
}