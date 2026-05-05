package com.wolfpack.data.local

import org.junit.Assert.*
import org.junit.Test

class TokenManagerTest {

    @Test
    fun `sesion no ha expirado si loginTime es reciente`() {
        val loginTime = System.currentTimeMillis() - (1 * 60 * 60 * 1000L) // hace 1 hora
        assertFalse(TokenManager.isSessionExpired(loginTime))
    }

    @Test
    fun `sesion ha expirado si loginTime es hace mas de 3 horas`() {
        val loginTime = System.currentTimeMillis() - (4 * 60 * 60 * 1000L) // hace 4 horas
        assertTrue(TokenManager.isSessionExpired(loginTime))
    }

    @Test
    fun `sesion ha expirado si loginTime es exactamente 3 horas`() {
        val loginTime = System.currentTimeMillis() - (3 * 60 * 60 * 1000L)
        assertTrue(TokenManager.isSessionExpired(loginTime))
    }
}