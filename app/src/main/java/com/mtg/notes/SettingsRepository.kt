package com.mtg.notes

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val IS_SORT_ASCENDING = booleanPreferencesKey("is_sort_ascending")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val SHOW_FAVORITES_ONLY = booleanPreferencesKey("show_favorites_only")
    }

    val userNameFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_NAME] ?: ""
    }

    val isSortAscendingFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_SORT_ASCENDING] ?: true
    }

    val isDarkThemeFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_DARK_THEME] ?: true
    }

    val showFavoritesOnlyFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_FAVORITES_ONLY] ?: false
    }

    suspend fun saveUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }

    suspend fun toggleSortOrder() {
        dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.IS_SORT_ASCENDING] ?: true
            preferences[PreferencesKeys.IS_SORT_ASCENDING] = !current
        }
    }

    suspend fun toggleTheme() {
        dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.IS_DARK_THEME] ?: false
            preferences[PreferencesKeys.IS_DARK_THEME] = !current
        }
    }

    suspend fun toggleFavoritesOnly() {
        dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.SHOW_FAVORITES_ONLY] ?: false
            preferences[PreferencesKeys.SHOW_FAVORITES_ONLY] = !current
        }
    }
}

lateinit var globalSettingsRepository: SettingsRepository