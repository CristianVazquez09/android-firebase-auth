package com.wolfpack.data.model

import java.io.Serializable

data class Materia(
    val uuid: String = "",
    val nombre: String = "",
    val activo: Boolean = true,
    val userId: String = "",
    val color: String = "#2196F3",
    val icono: String = "book"
) : Serializable {
    fun toMap(): Map<String, Any> = mapOf(
        "uuid" to uuid,
        "nombre" to nombre,
        "activo" to activo,
        "userId" to userId,
        "color" to color,
        "icono" to icono
    )
    companion object {
        fun fromMap(map: Map<String, Any?>): Materia = Materia(
            uuid = map["uuid"] as? String ?: "",
            nombre = map["nombre"] as? String ?: "",
            activo = map["activo"] as? Boolean ?: true,
            userId = map["userId"] as? String ?: "",
            color = map["color"] as? String ?: "#2196F3",
            icono = map["icono"] as? String ?: "book"
        )
    }
}
