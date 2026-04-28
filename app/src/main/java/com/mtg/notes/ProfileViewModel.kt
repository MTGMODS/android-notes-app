package com.mtg.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val repository = globalSettingsRepository

    val userName: StateFlow<String> = repository.userNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val isDarkTheme: StateFlow<Boolean> = repository.isDarkThemeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isSortAscending: StateFlow<Boolean> = repository.isSortAscendingFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val showFavoritesOnly: StateFlow<Boolean> = repository.showFavoritesOnlyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


    fun updateName(newName: String) {
        viewModelScope.launch { repository.saveUserName(newName) }
    }

    fun toggleTheme() {
        viewModelScope.launch { repository.toggleTheme() }
    }

    fun toggleSortOrder() {
        viewModelScope.launch { repository.toggleSortOrder() }
    }

    fun toggleFavoritesOnly() {
        viewModelScope.launch { repository.toggleFavoritesOnly() }
    }
}