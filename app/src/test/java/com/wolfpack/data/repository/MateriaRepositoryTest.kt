package com.wolfpack.data.repository

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import com.wolfpack.data.model.Materia
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class MateriaRepositoryTest {

    @Mock lateinit var firestore: FirebaseFirestore
    @Mock lateinit var usersCol: CollectionReference
    @Mock lateinit var userDoc: DocumentReference
    @Mock lateinit var materiasCol: CollectionReference
    @Mock lateinit var docRef: DocumentReference

    private lateinit var repo: MateriaRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(firestore.collection("users")).thenReturn(usersCol)
        `when`(usersCol.document("u1")).thenReturn(userDoc)
        `when`(userDoc.collection("materias")).thenReturn(materiasCol)
        `when`(materiasCol.document("m1")).thenReturn(docRef)
        repo = MateriaRepository(firestore)
    }

    @Test
    fun `saveMateria calls set on document`() = runTest {
        val materia = Materia(uuid = "m1", nombre = "Física", activo = true, userId = "u1")
        `when`(docRef.set(materia.toMap())).thenReturn(Tasks.forResult(null))

        val result = repo.saveMateria("u1", materia)

        assertTrue(result.isSuccess)
        verify(docRef).set(materia.toMap())
    }

    @Test
    fun `deleteMateria calls delete on document`() = runTest {
        `when`(docRef.delete()).thenReturn(Tasks.forResult(null))

        val result = repo.deleteMateria("u1", "m1")

        assertTrue(result.isSuccess)
        verify(docRef).delete()
    }
}