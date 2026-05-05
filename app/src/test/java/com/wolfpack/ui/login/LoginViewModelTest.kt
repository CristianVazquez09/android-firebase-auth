package com.wolfpack.ui.login

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        viewModel = LoginViewModel()
    }

    @Test
    fun `validateInputs devuelve false si email esta vacio`() {
        val result = viewModel.validateInputs("", "password123")
        assertFalse(result)
    }

    @Test
    fun `validateInputs devuelve false si password esta vacia`() {
        val result = viewModel.validateInputs("test@test.com", "")
        assertFalse(result)
    }

    @Test
    fun `validateInputs devuelve true con email y password validos`() {
        val result = viewModel.validateInputs("test@test.com", "password123")
        assertTrue(result)
    }
}