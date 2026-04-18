package com.mtg.notes

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    fun setInitialNameIfNeeded(name: String) {
        if (_userName.value.isEmpty()) {
            _userName.value = name
        }
    }

    fun updateName(newName: String) {
        _userName.value = newName
    }
}