package com.wolfpack.data.model

import org.junit.Assert.*
import org.junit.Test

class UserProfileTest {

    @Test
    fun `UserProfile tiene todos los campos requeridos`() {
        val user = UserProfile(
            uid = "abc123",
            nombre = "Juan",
            apellido = "Pérez",
            telefono = "12345678",
            email = "juan@example.com"
        )
        assertEquals("abc123", user.uid)
        assertEquals("Juan", user.nombre)
        assertEquals("Pérez", user.apellido)
        assertEquals("12345678", user.telefono)
        assertEquals("juan@example.com", user.email)
    }

    @Test
    fun `UserProfile se puede convertir a Map para Firestore`() {
        val user = UserProfile(
            uid = "abc123",
            nombre = "Juan",
            apellido = "Pérez",
            telefono = "12345678",
            email = "juan@example.com"
        )
        val map = user.toMap()
        assertEquals("Juan", map["nombre"])
        assertEquals("Pérez", map["apellido"])
        assertEquals("12345678", map["telefono"])
        assertEquals("juan@example.com", map["email"])
    }
}