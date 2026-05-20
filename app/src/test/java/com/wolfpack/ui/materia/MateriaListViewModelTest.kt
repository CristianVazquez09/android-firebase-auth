package com.wolfpack.ui.materia

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.wolfpack.data.model.Materia
import com.wolfpack.data.repository.MateriaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class MateriaListViewModelTest {

    @get:Rule val instantRule = InstantTaskExecutorRule()

    @Mock lateinit var repo: MateriaRepository
    private lateinit var vm: MateriaListViewModel
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(dispatcher)
        vm = MateriaListViewModel(repo)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `loadMaterias updates materias LiveData`() = runTest {
        val list = listOf(Materia(uuid = "m1", nombre = "Física", activo = true, userId = "u1"))
        `when`(repo.getMaterias("u1")).thenReturn(Result.success(list))

        vm.loadMaterias("u1")
        advanceUntilIdle()

        assertEquals(list, vm.materias.value)
    }

    @Test
    fun `loadMaterias sets error on failure`() = runTest {
        `when`(repo.getMaterias("u1")).thenReturn(Result.failure(Exception("error")))

        vm.loadMaterias("u1")
        advanceUntilIdle()

        assertNotNull(vm.error.value)
    }
}