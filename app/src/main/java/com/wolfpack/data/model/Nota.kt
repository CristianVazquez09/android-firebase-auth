package com.wolfpack.data.model

import java.io.Serializable

data class Nota(
    val uuid: String = "",
    val titulo: String = "",
    val contenido: String = "",
    val materiaId: String = "",
    val userId: String = "",
    val fechaCreacion: Long = 0L,
    val fechaModificacion: Long = 0L,
    val favorita: Boolean = false,
    val fechaRecordatorio: Long = 0L
) : Serializable {
    fun toMap(): Map<String, Any> = mapOf(
        "uuid" to uuid,
        "titulo" to titulo,
        "contenido" to contenido,
        "materiaId" to materiaId,
        "userId" to userId,
        "fechaCreacion" to fechaCreacion,
        "fechaModificacion" to fechaModificacion,
        "favorita" to favorita,
        "fechaRecordatorio" to fechaRecordatorio
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Nota = Nota(
            uuid = map["uuid"] as? String ?: "",
            titulo = map["titulo"] as? String ?: "",
            contenido = map["contenido"] as? String ?: "",
            materiaId = map["materiaId"] as? String ?: "",
            userId = map["userId"] as? String ?: "",
            fechaCreacion = (map["fechaCreacion"] as? Number)?.toLong() ?: 0L,
            fechaModificacion = (map["fechaModificacion"] as? Number)?.toLong() ?: 0L,
            favorita = map["favorita"] as? Boolean ?: false,
            fechaRecordatorio = (map["fechaRecordatorio"] as? Number)?.toLong() ?: 0L
        )
    }
}
