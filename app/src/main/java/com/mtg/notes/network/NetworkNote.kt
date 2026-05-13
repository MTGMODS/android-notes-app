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
    @SerializedName("updatedAt") val updatedAt: Long?,
    @SerializedName("sourceUrl") val sourceUrl: String?,
    @SerializedName("estimatedHours") val estimatedHours: Int?,
    @SerializedName("priority") val priority: Int?,
    @SerializedName("imagePath") val imagePath: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?
)


fun NetworkNote.toLocalNote(): Note {
    return Note(
        id = this.id?.toIntOrNull() ?: 0,
        title = this.title ?: "Без назви",
        content = this.content ?: "",
        folder = try { this.folder?.let { Folder.valueOf(it) } } catch (e: Exception) { null },
        isFavorite = this.isFavorite ?: false,
        updatedAt = this.updatedAt ?: System.currentTimeMillis(),
        sourceUrl = this.sourceUrl ?: "",
        estimatedHours = this.estimatedHours ?: 0,
        priority = this.priority ?: 1,
        imagePath = this.imagePath,
        latitude = this.latitude,
        longitude = this.longitude
    )
}

fun Note.toNetworkNote(): NetworkNote {
    return NetworkNote(
        title = this.title,
        content = this.content,
        folder = this.folder?.name,
        isFavorite = this.isFavorite,
        updatedAt = this.updatedAt,
        sourceUrl = this.sourceUrl,
        estimatedHours = this.estimatedHours,
        priority = this.priority,
        imagePath = this.imagePath,
        latitude = this.latitude,
        longitude = this.longitude
    )
}