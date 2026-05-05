package com.wolfpack.ui.register

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RegisterViewModelTest {

    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setup() {
        viewModel = RegisterViewModel()
    }

    @Test
    fun `validateInputs devuelve false si nombre esta vacio`() {
        val result = viewModel.validateInputs("", "Perez", "12345678", "a@a.com", "pass123")
        assertFalse(result)
    }

    @Test
    fun `validateInputs devuelve false si password tiene menos de 6 caracteres`() {
        val result = viewModel.validateInputs("Juan", "Perez", "12345678", "a@a.com", "12345")
        assertFalse(result)
    }

    @Test
    fun `validateInputs devuelve false si email es invalido`() {
        val result = viewModel.validateInputs("Juan", "Perez", "12345678", "noesemail", "pass123")
        assertFalse(result)
    }

    @Test
    fun `validateInputs devuelve true con todos los campos validos`() {
        val result = viewModel.validateInputs("Juan", "Perez", "12345678", "juan@test.com", "pass123")
        assertTrue(result)
    }
}