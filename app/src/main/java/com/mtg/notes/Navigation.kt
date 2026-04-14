package com.mtg.notes

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object NameInput : Screen("name_input")
    object Main : Screen("main/{userName}") {
        fun createRoute(userName: String) = "main/$userName"
    }

    object NoteDetails : Screen("details/{noteId}") {
        fun createRoute(noteId: Int) = "details/$noteId"
    }
}