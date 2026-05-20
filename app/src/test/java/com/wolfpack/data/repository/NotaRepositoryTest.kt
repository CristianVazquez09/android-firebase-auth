package com.wolfpack.data.repository

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import com.wolfpack.data.model.Nota
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class NotaRepositoryTest {

    @Mock lateinit var firestore: FirebaseFirestore
    @Mock lateinit var usersCol: CollectionReference
    @Mock lateinit var userDoc: DocumentReference
    @Mock lateinit var notasCol: CollectionReference
    @Mock lateinit var docRef: DocumentReference

    private lateinit var repo: NotaRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(firestore.collection("users")).thenReturn(usersCol)
        `when`(usersCol.document("u1")).thenReturn(userDoc)
        `when`(userDoc.collection("notas")).thenReturn(notasCol)
        `when`(notasCol.document("n1")).thenReturn(docRef)
        repo = NotaRepository(firestore)
    }

    @Test
    fun `saveNota calls set on document`() = runTest {
        val nota = Nota(uuid = "n1", titulo = "T", userId = "u1", fechaCreacion = 100L, fechaModificacion = 100L)
        `when`(docRef.set(nota.toMap())).thenReturn(Tasks.forResult(null))

        val result = repo.saveNota("u1", nota)

        assertTrue(result.isSuccess)
        verify(docRef).set(nota.toMap())
    }

    @Test
    fun `deleteNota calls delete on document`() = runTest {
        `when`(docRef.delete()).thenReturn(Tasks.forResult(null))

        val result = repo.deleteNota("u1", "n1")

        assertTrue(result.isSuccess)
        verify(docRef).delete()
    }
}