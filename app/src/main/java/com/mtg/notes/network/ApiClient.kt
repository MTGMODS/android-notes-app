package com.mtg.notes.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://69f0edc5c1533dbedc9df3e4.mockapi.io/api/v1/"

    val apiService: NoteApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NoteApiService::class.java)
    }
}