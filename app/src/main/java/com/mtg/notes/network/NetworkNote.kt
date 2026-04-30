package com.mtg.notes.network

import com.google.gson.annotations.SerializedName
import com.mtg.notes.Folder
import com.mtg.notes.Note

data class NetworkNote(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("folder") val folder: String?,
    @SerializedName("isFavorite") val isFavorite: Boolean?,
    @SerializedName("updatedAt") val updatedAt: Long?
)


fun NetworkNote.toLocalNote(): Note {
    return Note(
        id = this.id?.toIntOrNull() ?: 0,
        title = this.title ?: "Без назви",
        content = this.content ?: "",
        folder = try { this.folder?.let { Folder.valueOf(it) } } catch (e: Exception) { null },
        isFavorite = this.isFavorite ?: false,
        updatedAt = this.updatedAt ?: System.currentTimeMillis()
    )
}

fun Note.toNetworkNote(): NetworkNote {
    return NetworkNote(
        title = this.title,
        content = this.content,
        folder = this.folder?.name,
        isFavorite = this.isFavorite,
        updatedAt = this.updatedAt
    )
}