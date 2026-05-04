package com.wolfpack.data.model

data class UserProfile(
    val uid: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val telefono: String = "",
    val email: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "nombre" to nombre,
        "apellido" to apellido,
        "telefono" to telefono,
        "email" to email
    )

    companion object {
        fun fromMap(uid: String, map: Map<String, Any>): UserProfile = UserProfile(
            uid = uid,
            nombre = map["nombre"] as? String ?: "",
            apellido = map["apellido"] as? String ?: "",
            telefono = map["telefono"] as? String ?: "",
            email = map["email"] as? String ?: ""
        )
    }
}