package com.wolfpack.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Materia(
    val uuid: String = "",
    val nombre: String = "",
    val activo: Boolean = true,
    val userId: String = ""
) : Parcelable {
    fun toMap(): Map<String, Any> = mapOf(
        "uuid" to uuid,
        "nombre" to nombre,
        "activo" to activo,
        "userId" to userId
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Materia = Materia(
            uuid = map["uuid"] as? String ?: "",
            nombre = map["nombre"] as? String ?: "",
            activo = map["activo"] as? Boolean ?: true,
            userId = map["userId"] as? String ?: ""
        )
    }
}