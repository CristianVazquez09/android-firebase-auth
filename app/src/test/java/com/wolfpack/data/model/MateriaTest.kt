package com.wolfpack.data.model

import org.junit.Assert.*
import org.junit.Test

class MateriaTest {

    @Test
    fun `toMap includes all fields`() {
        val m = Materia(uuid = "abc", nombre = "Matemáticas", activo = true, userId = "u1")
        val map = m.toMap()
        assertEquals("abc", map["uuid"])
        assertEquals("Matemáticas", map["nombre"])
        assertEquals(true, map["activo"])
        assertEquals("u1", map["userId"])
    }

    @Test
    fun `fromMap reconstructs Materia`() {
        val map = mapOf("uuid" to "abc", "nombre" to "Historia", "activo" to false, "userId" to "u2")
        val m = Materia.fromMap(map)
        assertEquals("abc", m.uuid)
        assertEquals("Historia", m.nombre)
        assertEquals(false, m.activo)
        assertEquals("u2", m.userId)
    }

    @Test
    fun `fromMap uses defaults for missing fields`() {
        val m = Materia.fromMap(emptyMap())
        assertEquals("", m.uuid)
        assertEquals("", m.nombre)
        assertEquals(true, m.activo)
        assertEquals("", m.userId)
    }
}