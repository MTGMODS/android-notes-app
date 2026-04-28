package com.mtg.notes

import androidx.lifecycle.lifecycleScope
import androidx.activity.enableEdgeToEdge
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mtg.notes.ui.theme.NotesTheme
import androidx.compose.foundation.isSystemInDarkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = NotesDatabase.getDatabase(applicationContext, lifecycleScope)
        globalNotesRepository = NotesRepository(database.noteDao())

        globalSettingsRepository = SettingsRepository(applicationContext.dataStore)

        setContent {
            val isDarkTheme by globalSettingsRepository.isDarkThemeFlow.collectAsState(initial = isSystemInDarkTheme())
            NotesTheme(darkTheme = isDarkTheme) {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    val savedUserName by globalSettingsRepository.userNameFlow.collectAsState(initial = null)
    if (savedUserName == null) return

    val startDestination = if (savedUserName!!.isNotBlank()) Screen.Main.createRoute(savedUserName!!) else Screen.Onboarding.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Onboarding.route) { backStackEntry ->
            val savedName by backStackEntry.savedStateHandle.getStateFlow("userName", "").collectAsState()
            OnboardingScreen(
                savedName = savedName,
                onEnterNameClick = { navController.navigate(Screen.NameInput.route) },
                onStartClick = { name ->
                    profileViewModel.updateName(name)
                    navController.navigate(Screen.Main.createRoute(name)) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.NameInput.route) {
            NameInputScreen(onSave = { name ->
                navController.previousBackStackEntry?.savedStateHandle?.set("userName", name)
                navController.popBackStack()
            })
        }

        composable(Screen.Main.route) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: "Гість"
            MainTabScreen(
                userName = userName,
                globalNavController = navController,
                mainViewModel = mainViewModel,
                profileViewModel = profileViewModel
            )
        }

        composable(
            route = Screen.NoteDetails.route,
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
            NoteEditorOverlay(
                noteId = noteId,
                onExit = { navController.popBackStack() }
            )
        }
    }
}