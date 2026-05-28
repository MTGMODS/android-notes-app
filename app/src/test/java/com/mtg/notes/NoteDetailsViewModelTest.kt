package com.mtg.notes

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: NotesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Позитивний сценарій збереження нової нотатки`() = runTest {
        val viewModel = NoteDetailsViewModel(noteId = -1, repository = mockRepository)
        coEvery { mockRepository.addNote(any()) } returns Result.success(mockk<Note>(relaxed = true))

        viewModel.updateState { it.copy(title = "Тест", content = "Опис", folder = Folder.WORK) }
        viewModel.saveNote()
        advanceUntilIdle()

        assertEquals(NoteDetailsState.Saved, viewModel.uiState.value)
    }

    @Test
    fun `Негативний сценарій з порожнім заголовком`() = runTest {
        val viewModel = NoteDetailsViewModel(noteId = -1, repository = mockRepository)
        viewModel.updateState { it.copy(title = "", content = "Опис", folder = Folder.WORK) }

        viewModel.saveNote()

        val state = viewModel.uiState.value as NoteDetailsState.Editing
        assertEquals("Поле не може бути порожнім", state.formState.titleError)
        coVerify(exactly = 0) { mockRepository.addNote(any()) }
    }

    @Test
    fun `Edge case помилка завантаження неіснуючої нотатки`() = runTest {
        coEvery { mockRepository.getNoteById(-999) } returns null
        val viewModel = NoteDetailsViewModel(noteId = -999, repository = mockRepository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is NoteDetailsState.Error)
    }
}