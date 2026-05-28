@file:Suppress("DEPRECATION")

package com.mtg.notes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class NoteCreateUITest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testCreateNewNoteFlow() {

        composeTestRule.onNodeWithContentDescription("Додати нотатку").performClick()


        composeTestRule.onNodeWithText("Заголовок").performTextInput("UI Тест")
        composeTestRule.onNodeWithText("Текст нотатки").performTextInput("Працює ідеально!")


        composeTestRule.onNodeWithContentDescription("Зберегти").performClick()

        composeTestRule.onNodeWithText("UI Тест").assertIsDisplayed()
    }
}