package com.wolfpack.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.wolfpack.data.model.Materia
import kotlinx.coroutines.tasks.await

class MateriaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun col(userId: String) =
        firestore.collection("users").document(userId).collection("materias")

    suspend fun saveMateria(userId: String, materia: Materia): Result<Unit> = runCatching {
        col(userId).document(materia.uuid).set(materia.toMap()).await()
    }

    suspend fun getMaterias(userId: String): Result<List<Materia>> = runCatching {
        col(userId).get().await().documents.map { doc ->
            Materia.fromMap(doc.data ?: emptyMap())
        }
    }

    suspend fun deleteMateria(userId: String, uuid: String): Result<Unit> = runCatching {
        col(userId).document(uuid).delete().await()
    }
}