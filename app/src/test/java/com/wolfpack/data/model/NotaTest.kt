package com.wolfpack.data.model

import org.junit.Assert.*
import org.junit.Test

class NotaTest {

    @Test
    fun `toMap includes all fields`() {
        val n = Nota(
            uuid = "n1", titulo = "Apunte 1", contenido = "Texto",
            materiaId = "m1", userId = "u1",
            fechaCreacion = 1000L, fechaModificacion = 2000L
        )
        val map = n.toMap()
        assertEquals("n1", map["uuid"])
        assertEquals("Apunte 1", map["titulo"])
        assertEquals("Texto", map["contenido"])
        assertEquals("m1", map["materiaId"])
        assertEquals("u1", map["userId"])
        assertEquals(1000L, map["fechaCreacion"])
        assertEquals(2000L, map["fechaModificacion"])
    }

    @Test
    fun `fromMap reconstructs Nota`() {
        val map = mapOf(
            "uuid" to "n1", "titulo" to "T", "contenido" to "C",
            "materiaId" to "m1", "userId" to "u1",
            "fechaCreacion" to 100L, "fechaModificacion" to 200L
        )
        val n = Nota.fromMap(map)
        assertEquals("n1", n.uuid)
        assertEquals("T", n.titulo)
        assertEquals(100L, n.fechaCreacion)
        assertEquals("C", n.contenido)
        assertEquals("m1", n.materiaId)
        assertEquals("u1", n.userId)
        assertEquals(200L, n.fechaModificacion)
    }

    @Test
    fun `fromMap uses defaults for missing fields`() {
        val n = Nota.fromMap(emptyMap())
        assertEquals("", n.uuid)
        assertEquals("", n.titulo)
        assertEquals(0L, n.fechaCreacion)
    }
}