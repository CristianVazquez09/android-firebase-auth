package com.wolfpack.ui.nota

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.wolfpack.data.model.Nota
import com.wolfpack.data.repository.NotaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class NotaListViewModelTest {

    @get:Rule val instantRule = InstantTaskExecutorRule()

    @Mock lateinit var repo: NotaRepository
    private lateinit var vm: NotaListViewModel
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(dispatcher)
        vm = NotaListViewModel(repo)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `loadNotas updates notas LiveData`() = runTest {
        val list = listOf(Nota(uuid = "n1", titulo = "Apunte 1", userId = "u1"))
        `when`(repo.getNotas("u1")).thenReturn(Result.success(list))

        vm.loadNotas("u1")
        advanceUntilIdle()

        assertEquals(list, vm.notas.value)
    }

    @Test
    fun `loadNotas sets error on failure`() = runTest {
        `when`(repo.getNotas("u1")).thenReturn(Result.failure(Exception("fail")))

        vm.loadNotas("u1")
        advanceUntilIdle()

        assertNotNull(vm.error.value)
    }
}