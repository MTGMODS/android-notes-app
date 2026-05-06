package com.mtg.notes.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface NoteApiService {

    @GET("notes")
    suspend fun getAllNotes(): List<NetworkNote>

    @GET("notes/{id}")
    suspend fun getNoteById(@Path("id") id: String): NetworkNote

    @POST("notes")
    suspend fun createNote(@Body note: NetworkNote): NetworkNote

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: String)

    @PUT("notes/{id}")
    suspend fun updateNote(@Path("id") id: String, @Body note: NetworkNote): NetworkNote


}