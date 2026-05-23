package com.wolfpack.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.wolfpack.data.model.Nota
import kotlinx.coroutines.tasks.await

class NotaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun col(userId: String) =
        firestore.collection("users").document(userId).collection("notas")

    suspend fun saveNota(userId: String, nota: Nota): Result<Unit> = runCatching {
        col(userId).document(nota.uuid).set(nota.toMap()).await()
    }

    suspend fun getNotas(userId: String): Result<List<Nota>> = runCatching {
        col(userId).get().await().documents.map { doc ->
            Nota.fromMap(doc.data ?: emptyMap())
        }
    }

    suspend fun deleteNota(userId: String, uuid: String): Result<Unit> = runCatching {
        col(userId).document(uuid).delete().await()
    }

    suspend fun updateFavorita(userId: String, uuid: String, favorita: Boolean): Result<Unit> =
        runCatching {
            col(userId).document(uuid).update("favorita", favorita).await()
        }
}

