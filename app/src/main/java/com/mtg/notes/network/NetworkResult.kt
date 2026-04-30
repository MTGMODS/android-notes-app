package com.mtg.notes.network

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val isOffline: Boolean = false) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}