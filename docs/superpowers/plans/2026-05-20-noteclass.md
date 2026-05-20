# NoteClass – Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convertir la app de autenticación existente en "NoteClass", una app de apuntes para estudiantes donde cada usuario ve sus propias notas y materias almacenadas en Firestore.

**Architecture:** MVVM con ViewModels, LiveData y Coroutines; Firestore como única fuente de verdad usando subcolecciones por usuario (`users/{uid}/notas` y `users/{uid}/materias`); sin Room ni NavGraph, navegación por Intents directos igual que el resto del proyecto.

**Tech Stack:** Kotlin, Android ViewBinding, Material Design 3, Firebase Firestore, Coroutines, JUnit 4 + Mockito.

> **Nota:** Firebase Storage eliminado del alcance (plan gratuito). Las notas NO tienen campo foto.

---

## File Map

### Nuevos archivos
| Archivo | Responsabilidad |
|---|---|
| `data/model/Materia.kt` | Data class Materia con toMap/fromMap + Parcelable |
| `data/model/Nota.kt` | Data class Nota con toMap/fromMap + Parcelable |
| `data/repository/MateriaRepository.kt` | CRUD Firestore para `users/{uid}/materias` |
| `data/repository/NotaRepository.kt` | CRUD Firestore para `users/{uid}/notas` |
| `ui/materia/MateriaListActivity.kt` | Lista de materias con FAB para agregar |
| `ui/materia/MateriaListViewModel.kt` | LiveData<List<Materia>> + delete + saveMateria |
| `ui/materia/MateriaAdapter.kt` | RecyclerView adapter con toggle activo y delete |
| `ui/materia/MateriaFormActivity.kt` | Crear / editar materia |
| `ui/materia/MateriaFormViewModel.kt` | Guardar materia via repository |
| `ui/nota/NotaListActivity.kt` | Lista de notas del usuario con FAB para agregar |
| `ui/nota/NotaListViewModel.kt` | LiveData<List<Nota>> + delete |
| `ui/nota/NotaAdapter.kt` | RecyclerView adapter para notas |
| `ui/nota/NotaFormActivity.kt` | Crear / editar nota (título, contenido, materia) |
| `ui/nota/NotaFormViewModel.kt` | Guardar nota via repository |
| `res/layout/activity_nota_list.xml` | Layout lista notas |
| `res/layout/item_nota.xml` | Item RecyclerView nota |
| `res/layout/activity_nota_form.xml` | Formulario de nota |
| `res/layout/activity_materia_list.xml` | Layout lista materias |
| `res/layout/item_materia.xml` | Item RecyclerView materia |
| `res/layout/activity_materia_form.xml` | Formulario de materia |
| `test/.../MateriaRepositoryTest.kt` | Unit tests MateriaRepository |
| `test/.../NotaRepositoryTest.kt` | Unit tests NotaRepository |
| `test/.../MateriaListViewModelTest.kt` | Unit tests MateriaListViewModel |
| `test/.../NotaListViewModelTest.kt` | Unit tests NotaListViewModel |

### Archivos modificados
| Archivo | Cambio |
|---|---|
| `AndroidManifest.xml` | Registrar 4 nuevas activities |
| `ui/home/HomeActivity.kt` | Botones para ir a NotaListActivity y MateriaListActivity |
| `res/layout/activity_home.xml` | Agregar dos MaterialButtons de navegación |
| `res/values/strings.xml` | Nuevas cadenas de texto |
| `res/drawable/` | Agregar ic_arrow_back.xml |

---

## Task 1: Modelos de datos

**Files:**
- Create: `app/src/main/java/com/wolfpack/data/model/Materia.kt`
- Create: `app/src/main/java/com/wolfpack/data/model/Nota.kt`
- Create: `app/src/test/java/com/wolfpack/data/model/MateriaTest.kt`
- Create: `app/src/test/java/com/wolfpack/data/model/NotaTest.kt`

- [ ] **Step 1: Escribir tests para Materia**

`app/src/test/java/com/wolfpack/data/model/MateriaTest.kt`:
```kotlin
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
```

- [ ] **Step 2: Ejecutar test (debe fallar)**

```
./gradlew :app:testDebugUnitTest --tests "com.wolfpack.data.model.MateriaTest"
```
Esperado: FAILED – `Materia` no existe.

- [ ] **Step 3: Crear Materia.kt**

`app/src/main/java/com/wolfpack/data/model/Materia.kt`:
```kotlin
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
```

- [ ] **Step 4: Escribir tests para Nota**

`app/src/test/java/com/wolfpack/data/model/NotaTest.kt`:
```kotlin
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
    }

    @Test
    fun `fromMap uses defaults for missing fields`() {
        val n = Nota.fromMap(emptyMap())
        assertEquals("", n.uuid)
        assertEquals("", n.titulo)
        assertEquals(0L, n.fechaCreacion)
    }
}
```

- [ ] **Step 5: Crear Nota.kt**

`app/src/main/java/com/wolfpack/data/model/Nota.kt`:
```kotlin
package com.wolfpack.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Nota(
    val uuid: String = "",
    val titulo: String = "",
    val contenido: String = "",
    val materiaId: String = "",
    val userId: String = "",
    val fechaCreacion: Long = 0L,
    val fechaModificacion: Long = 0L
) : Parcelable {
    fun toMap(): Map<String, Any> = mapOf(
        "uuid" to uuid,
        "titulo" to titulo,
        "contenido" to contenido,
        "materiaId" to materiaId,
        "userId" to userId,
        "fechaCreacion" to fechaCreacion,
        "fechaModificacion" to fechaModificacion
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Nota = Nota(
            uuid = map["uuid"] as? String ?: "",
            titulo = map["titulo"] as? String ?: "",
            contenido = map["contenido"] as? String ?: "",
            materiaId = map["materiaId"] as? String ?: "",
            userId = map["userId"] as? String ?: "",
            fechaCreacion = map["fechaCreacion"] as? Long ?: 0L,
            fechaModificacion = map["fechaModificacion"] as? Long ?: 0L
        )
    }
}
```

- [ ] **Step 6: Habilitar plugin parcelize en build.gradle.kts**

Agregar `id("kotlin-parcelize")` en el bloque `plugins` de `app/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}
```

- [ ] **Step 7: Ejecutar tests de modelos**

```
./gradlew :app:testDebugUnitTest --tests "com.wolfpack.data.model.*"
```
Esperado: 6 tests PASSED.

- [ ] **Step 8: Comando de commit para el usuario**

```bash
git add app/src/main/java/com/wolfpack/data/model/Materia.kt \
        app/src/main/java/com/wolfpack/data/model/Nota.kt \
        app/src/test/java/com/wolfpack/data/model/MateriaTest.kt \
        app/src/test/java/com/wolfpack/data/model/NotaTest.kt \
        app/build.gradle.kts
git commit -m "funcionalidad: agregar modelos Materia y Nota con tests"
```

---

## Task 2: MateriaRepository

**Files:**
- Create: `app/src/main/java/com/wolfpack/data/repository/MateriaRepository.kt`
- Create: `app/src/test/java/com/wolfpack/data/repository/MateriaRepositoryTest.kt`

- [ ] **Step 1: Escribir tests**

`app/src/test/java/com/wolfpack/data/repository/MateriaRepositoryTest.kt`:
```kotlin
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
```

- [ ] **Step 2: Ejecutar test (debe fallar)**

```
./gradlew :app:testDebugUnitTest --tests "com.wolfpack.data.repository.MateriaRepositoryTest"
```
Esperado: FAILED – `MateriaRepository` no existe.

- [ ] **Step 3: Crear MateriaRepository.kt**

`app/src/main/java/com/wolfpack/data/repository/MateriaRepository.kt`:
```kotlin
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
```

- [ ] **Step 4: Ejecutar tests**

```
./gradlew :app:testDebugUnitTest --tests "com.wolfpack.data.repository.MateriaRepositoryTest"
```
Esperado: 2 tests PASSED.

- [ ] **Step 5: Comando de commit para el usuario**

```bash
git add app/src/main/java/com/wolfpack/data/repository/MateriaRepository.kt \
        app/src/test/java/com/wolfpack/data/repository/MateriaRepositoryTest.kt
git commit -m "funcionalidad: agregar MateriaRepository con CRUD en Firestore"
```

---

## Task 3: NotaRepository

**Files:**
- Create: `app/src/main/java/com/wolfpack/data/repository/NotaRepository.kt`
- Create: `app/src/test/java/com/wolfpack/data/repository/NotaRepositoryTest.kt`

- [ ] **Step 1: Escribir tests**

`app/src/test/java/com/wolfpack/data/repository/NotaRepositoryTest.kt`:
```kotlin
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
```

- [ ] **Step 2: Ejecutar test (debe fallar)**

```
./gradlew :app:testDebugUnitTest --tests "com.wolfpack.data.repository.NotaRepositoryTest"
```
Esperado: FAILED.

- [ ] **Step 3: Crear NotaRepository.kt**

`app/src/main/java/com/wolfpack/data/repository/NotaRepository.kt`:
```kotlin
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
}
```

- [ ] **Step 4: Ejecutar tests**

```
./gradlew :app:testDebugUnitTest --tests "com.wolfpack.data.repository.NotaRepositoryTest"
```
Esperado: 2 tests PASSED.

- [ ] **Step 5: Comando de commit para el usuario**

```bash
git add app/src/main/java/com/wolfpack/data/repository/NotaRepository.kt \
        app/src/test/java/com/wolfpack/data/repository/NotaRepositoryTest.kt
git commit -m "funcionalidad: agregar NotaRepository con CRUD en Firestore"
```

---

## Task 4: Materia UI – Lista

**Files:**
- Create: `app/src/main/java/com/wolfpack/ui/materia/MateriaListViewModel.kt`
- Create: `app/src/main/java/com/wolfpack/ui/materia/MateriaAdapter.kt`
- Create: `app/src/main/java/com/wolfpack/ui/materia/MateriaListActivity.kt`
- Create: `app/src/main/res/layout/activity_materia_list.xml`
- Create: `app/src/main/res/layout/item_materia.xml`
- Create: `app/src/test/java/com/wolfpack/ui/materia/MateriaListViewModelTest.kt`

- [ ] **Step 1: Escribir tests del ViewModel**

`app/src/test/java/com/wolfpack/ui/materia/MateriaListViewModelTest.kt`:
```kotlin
package com.wolfpack.ui.materia

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.wolfpack.data.model.Materia
import com.wolfpack.data.repository.MateriaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class MateriaListViewModelTest {

    @get:Rule val instantRule = InstantTaskExecutorRule()

    @Mock lateinit var repo: MateriaRepository
    private lateinit var vm: MateriaListViewModel
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(dispatcher)
        vm = MateriaListViewModel(repo)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `loadMaterias updates materias LiveData`() = runTest {
        val list = listOf(Materia(uuid = "m1", nombre = "Física", activo = true, userId = "u1"))
        `when`(repo.getMaterias("u1")).thenReturn(Result.success(list))

        vm.loadMaterias("u1")
        advanceUntilIdle()

        assertEquals(list, vm.materias.value)
    }

    @Test
    fun `loadMaterias sets error on failure`() = runTest {
        `when`(repo.getMaterias("u1")).thenReturn(Result.failure(Exception("error")))

        vm.loadMaterias("u1")
        advanceUntilIdle()

        assertNotNull(vm.error.value)
    }
}
```

- [ ] **Step 2: Ejecutar test (debe fallar)**

```
./gradlew :app:testDebugUnitTest --tests "com.wolfpack.ui.materia.MateriaListViewModelTest"
```
Esperado: FAILED.

- [ ] **Step 3: Crear MateriaListViewModel.kt**

`app/src/main/java/com/wolfpack/ui/materia/MateriaListViewModel.kt`:
```kotlin
package com.wolfpack.ui.materia

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wolfpack.data.model.Materia
import com.wolfpack.data.repository.MateriaRepository
import kotlinx.coroutines.launch

class MateriaListViewModel(
    private val repo: MateriaRepository = MateriaRepository()
) : ViewModel() {

    private val _materias = MutableLiveData<List<Materia>>()
    val materias: LiveData<List<Materia>> = _materias

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun loadMaterias(userId: String) {
        _loading.value = true
        viewModelScope.launch {
            repo.getMaterias(userId)
                .onSuccess { _materias.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun saveMateria(userId: String, materia: Materia) {
        viewModelScope.launch {
            repo.saveMateria(userId, materia)
                .onSuccess { loadMaterias(userId) }
                .onFailure { _error.value = it.message }
        }
    }

    fun deleteMateria(userId: String, uuid: String) {
        viewModelScope.launch {
            repo.deleteMateria(userId, uuid)
                .onSuccess { loadMaterias(userId) }
                .onFailure { _error.value = it.message }
        }
    }
}
```

- [ ] **Step 4: Ejecutar tests**

```
./gradlew :app:testDebugUnitTest --tests "com.wolfpack.ui.materia.MateriaListViewModelTest"
```
Esperado: 2 tests PASSED.

- [ ] **Step 5: Crear item_materia.xml**

`app/src/main/res/layout/item_materia.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="16dp"
    android:layout_marginVertical="6dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="2dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="16dp"
        android:gravity="center_vertical">

        <TextView
            android:id="@+id/tvNombreMateria"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:textSize="16sp"
            android:textColor="@color/text_primary"
            android:textStyle="bold" />

        <com.google.android.material.switchmaterial.SwitchMaterial
            android:id="@+id/switchActivo"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginEnd="8dp" />

        <ImageButton
            android:id="@+id/btnDeleteMateria"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:src="@android:drawable/ic_menu_delete"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:contentDescription="Eliminar materia"
            android:tint="@color/error" />
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

- [ ] **Step 6: Crear activity_materia_list.xml**

`app/src/main/res/layout/activity_materia_list.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background">

    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <com.google.android.material.appbar.MaterialToolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            app:title="Mis Materias"
            app:navigationIcon="@drawable/ic_arrow_back" />
    </com.google.android.material.appbar.AppBarLayout>

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/rvMaterias"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:paddingTop="8dp"
            android:clipToPadding="false" />

        <TextView
            android:id="@+id/tvEmpty"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:text="No tienes materias aún"
            android:textColor="@color/text_secondary"
            android:textSize="16sp"
            android:visibility="gone" />

        <ProgressBar
            android:id="@+id/progressBar"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:visibility="gone" />
    </FrameLayout>

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fabAddMateria"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="16dp"
        android:contentDescription="Agregar materia"
        app:srcCompat="@android:drawable/ic_input_add" />
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

- [ ] **Step 7: Crear MateriaAdapter.kt**

`app/src/main/java/com/wolfpack/ui/materia/MateriaAdapter.kt`:
```kotlin
package com.wolfpack.ui.materia

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wolfpack.data.model.Materia
import com.wolfpack.databinding.ItemMateriaBinding

class MateriaAdapter(
    private val onToggleActivo: (Materia, Boolean) -> Unit,
    private val onDelete: (Materia) -> Unit,
    private val onEdit: (Materia) -> Unit
) : ListAdapter<Materia, MateriaAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemMateriaBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(materia: Materia) {
            b.tvNombreMateria.text = materia.nombre
            b.switchActivo.isChecked = materia.activo
            b.switchActivo.setOnCheckedChangeListener { _, checked ->
                onToggleActivo(materia, checked)
            }
            b.btnDeleteMateria.setOnClickListener { onDelete(materia) }
            b.root.setOnClickListener { onEdit(materia) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemMateriaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Materia>() {
            override fun areItemsTheSame(a: Materia, b: Materia) = a.uuid == b.uuid
            override fun areContentsTheSame(a: Materia, b: Materia) = a == b
        }
    }
}
```

- [ ] **Step 8: Crear MateriaListActivity.kt**

`app/src/main/java/com/wolfpack/ui/materia/MateriaListActivity.kt`:
```kotlin
package com.wolfpack.ui.materia

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.snackbar.Snackbar
import com.wolfpack.databinding.ActivityMateriaListBinding
import com.wolfpack.ui.base.BaseActivity

class MateriaListActivity : BaseActivity() {

    private lateinit var binding: ActivityMateriaListBinding
    private val viewModel: MateriaListViewModel by viewModels()
    private lateinit var adapter: MateriaAdapter
    private val userId get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun requiresAuth() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMateriaListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = MateriaAdapter(
            onToggleActivo = { materia, checked ->
                viewModel.saveMateria(userId, materia.copy(activo = checked))
            },
            onDelete = { materia -> viewModel.deleteMateria(userId, materia.uuid) },
            onEdit = { materia ->
                startActivity(MateriaFormActivity.editIntent(this, materia))
            }
        )

        binding.rvMaterias.layoutManager = LinearLayoutManager(this)
        binding.rvMaterias.adapter = adapter
        binding.fabAddMateria.setOnClickListener {
            startActivity(Intent(this, MateriaFormActivity::class.java))
        }

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadMaterias(userId)
    }

    private fun observeViewModel() {
        viewModel.materias.observe(this) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.loading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(this) { err ->
            err?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
        }
    }
}
```

- [ ] **Step 9: Comando de commit para el usuario**

```bash
git add app/src/main/java/com/wolfpack/ui/materia/ \
        app/src/main/res/layout/activity_materia_list.xml \
        app/src/main/res/layout/item_materia.xml \
        app/src/test/java/com/wolfpack/ui/materia/MateriaListViewModelTest.kt
git commit -m "funcionalidad: agregar pantalla lista de materias con RecyclerView"
```

---

## Task 5: Materia UI – Formulario

**Files:**
- Create: `app/src/main/java/com/wolfpack/ui/materia/MateriaFormViewModel.kt`
- Create: `app/src/main/java/com/wolfpack/ui/materia/MateriaFormActivity.kt`
- Create: `app/src/main/res/layout/activity_materia_form.xml`

- [ ] **Step 1: Crear activity_materia_form.xml**

`app/src/main/res/layout/activity_materia_form.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/background">

    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        app:title="Materia"
        app:navigationIcon="@drawable/ic_arrow_back" />

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="24dp">

            <com.google.android.material.textfield.TextInputLayout
                android:id="@+id/tilNombreMateria"
                style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="Nombre de la materia">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/etNombreMateria"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:inputType="textCapSentences" />
            </com.google.android.material.textfield.TextInputLayout>

            <com.google.android.material.switchmaterial.SwitchMaterial
                android:id="@+id/switchActivoForm"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:checked="true"
                android:text="Materia activa" />

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btnGuardarMateria"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="32dp"
                android:text="Guardar"
                app:cornerRadius="12dp" />
        </LinearLayout>
    </ScrollView>
</LinearLayout>
```

- [ ] **Step 2: Crear MateriaFormViewModel.kt**

`app/src/main/java/com/wolfpack/ui/materia/MateriaFormViewModel.kt`:
```kotlin
package com.wolfpack.ui.materia

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wolfpack.data.model.Materia
import com.wolfpack.data.repository.MateriaRepository
import kotlinx.coroutines.launch
import java.util.UUID

class MateriaFormViewModel(
    private val repo: MateriaRepository = MateriaRepository()
) : ViewModel() {

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun saveMateria(userId: String, uuid: String?, nombre: String, activo: Boolean) {
        if (nombre.isBlank()) {
            _error.value = "El nombre no puede estar vacío"
            return
        }
        val materia = Materia(
            uuid = uuid ?: UUID.randomUUID().toString(),
            nombre = nombre.trim(),
            activo = activo,
            userId = userId
        )
        viewModelScope.launch {
            repo.saveMateria(userId, materia)
                .onSuccess { _saved.value = true }
                .onFailure { _error.value = it.message }
        }
    }
}
```

- [ ] **Step 3: Crear MateriaFormActivity.kt**

`app/src/main/java/com/wolfpack/ui/materia/MateriaFormActivity.kt`:
```kotlin
package com.wolfpack.ui.materia

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.wolfpack.data.model.Materia
import com.wolfpack.databinding.ActivityMateriaFormBinding
import com.wolfpack.ui.base.BaseActivity

class MateriaFormActivity : BaseActivity() {

    private lateinit var binding: ActivityMateriaFormBinding
    private val viewModel: MateriaFormViewModel by viewModels()
    private val userId get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var editMateria: Materia? = null

    override fun requiresAuth() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMateriaFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        editMateria = intent.getParcelableExtra(EXTRA_MATERIA)
        editMateria?.let { m ->
            binding.etNombreMateria.setText(m.nombre)
            binding.switchActivoForm.isChecked = m.activo
            binding.toolbar.title = "Editar Materia"
        }

        binding.btnGuardarMateria.setOnClickListener {
            viewModel.saveMateria(
                userId = userId,
                uuid = editMateria?.uuid,
                nombre = binding.etNombreMateria.text.toString(),
                activo = binding.switchActivoForm.isChecked
            )
        }

        viewModel.saved.observe(this) { if (it) finish() }
        viewModel.error.observe(this) { err ->
            err?.let { binding.tilNombreMateria.error = it }
        }
    }

    companion object {
        private const val EXTRA_MATERIA = "extra_materia"

        fun editIntent(context: Context, materia: Materia): Intent =
            Intent(context, MateriaFormActivity::class.java).putExtra(EXTRA_MATERIA, materia)
    }
}
```

- [ ] **Step 4: Comando de commit para el usuario**

```bash
git add app/src/main/java/com/wolfpack/ui/materia/MateriaFormActivity.kt \
        app/src/main/java/com/wolfpack/ui/materia/MateriaFormViewModel.kt \
        app/src/main/res/layout/activity_materia_form.xml
git commit -m "funcionalidad: agregar formulario crear/editar materia"
```

---

## Task 6: Nota UI – Lista

**Files:**
- Create: `app/src/main/java/com/wolfpack/ui/nota/NotaListViewModel.kt`
- Create: `app/src/main/java/com/wolfpack/ui/nota/NotaAdapter.kt`
- Create: `app/src/main/java/com/wolfpack/ui/nota/NotaListActivity.kt`
- Create: `app/src/main/res/layout/activity_nota_list.xml`
- Create: `app/src/main/res/layout/item_nota.xml`
- Create: `app/src/test/java/com/wolfpack/ui/nota/NotaListViewModelTest.kt`

- [ ] **Step 1: Escribir tests del ViewModel**

`app/src/test/java/com/wolfpack/ui/nota/NotaListViewModelTest.kt`:
```kotlin
package com.wolfpack.ui.nota

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.wolfpack.data.model.Nota
import com.wolfpack.data.repository.NotaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class NotaListViewModelTest {

    @get:Rule val instantRule = InstantTaskExecutorRule()

    @Mock lateinit var repo: NotaRepository
    private lateinit var vm: NotaListViewModel
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(dispatcher)
        vm = NotaListViewModel(repo)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `loadNotas updates notas LiveData`() = runTest {
        val list = listOf(Nota(uuid = "n1", titulo = "Apunte 1", userId = "u1"))
        `when`(repo.getNotas("u1")).thenReturn(Result.success(list))

        vm.loadNotas("u1")
        advanceUntilIdle()

        assertEquals(list, vm.notas.value)
    }

    @Test
    fun `loadNotas sets error on failure`() = runTest {
        `when`(repo.getNotas("u1")).thenReturn(Result.failure(Exception("fail")))

        vm.loadNotas("u1")
        advanceUntilIdle()

        assertNotNull(vm.error.value)
    }
}
```

- [ ] **Step 2: Ejecutar test (debe fallar)**

```
./gradlew :app:testDebugUnitTest --tests "com.wolfpack.ui.nota.NotaListViewModelTest"
```
Esperado: FAILED.

- [ ] **Step 3: Crear NotaListViewModel.kt**

`app/src/main/java/com/wolfpack/ui/nota/NotaListViewModel.kt`:
```kotlin
package com.wolfpack.ui.nota

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wolfpack.data.model.Nota
import com.wolfpack.data.repository.NotaRepository
import kotlinx.coroutines.launch

class NotaListViewModel(
    private val repo: NotaRepository = NotaRepository()
) : ViewModel() {

    private val _notas = MutableLiveData<List<Nota>>()
    val notas: LiveData<List<Nota>> = _notas

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun loadNotas(userId: String) {
        _loading.value = true
        viewModelScope.launch {
            repo.getNotas(userId)
                .onSuccess { _notas.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun deleteNota(userId: String, uuid: String) {
        viewModelScope.launch {
            repo.deleteNota(userId, uuid)
                .onSuccess { loadNotas(userId) }
                .onFailure { _error.value = it.message }
        }
    }
}
```

- [ ] **Step 4: Ejecutar tests**

```
./gradlew :app:testDebugUnitTest --tests "com.wolfpack.ui.nota.NotaListViewModelTest"
```
Esperado: 2 tests PASSED.

- [ ] **Step 5: Crear item_nota.xml**

`app/src/main/res/layout/item_nota.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="16dp"
    android:layout_marginVertical="6dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="2dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical">

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical">

                <TextView
                    android:id="@+id/tvTituloNota"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="@color/text_primary" />

                <TextView
                    android:id="@+id/tvMateriaChip"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp"
                    android:textSize="12sp"
                    android:textColor="@color/primary"
                    android:background="@drawable/bg_rounded_input"
                    android:paddingHorizontal="8dp"
                    android:paddingVertical="2dp" />
            </LinearLayout>

            <ImageButton
                android:id="@+id/btnDeleteNota"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="8dp"
                android:src="@android:drawable/ic_menu_delete"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:contentDescription="Eliminar nota"
                android:tint="@color/error" />
        </LinearLayout>

        <TextView
            android:id="@+id/tvFechaModificacion"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:textSize="11sp"
            android:textColor="@color/text_secondary" />
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

- [ ] **Step 6: Crear activity_nota_list.xml**

`app/src/main/res/layout/activity_nota_list.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background">

    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <com.google.android.material.appbar.MaterialToolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            app:title="Mis Notas"
            app:navigationIcon="@drawable/ic_arrow_back" />
    </com.google.android.material.appbar.AppBarLayout>

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/rvNotas"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:paddingTop="8dp"
            android:clipToPadding="false" />

        <TextView
            android:id="@+id/tvEmptyNotas"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:text="No tienes notas aún"
            android:textColor="@color/text_secondary"
            android:textSize="16sp"
            android:visibility="gone" />

        <ProgressBar
            android:id="@+id/progressBarNotas"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:visibility="gone" />
    </FrameLayout>

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fabAddNota"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="16dp"
        android:contentDescription="Agregar nota"
        app:srcCompat="@android:drawable/ic_input_add" />
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

- [ ] **Step 7: Crear NotaAdapter.kt**

`app/src/main/java/com/wolfpack/ui/nota/NotaAdapter.kt`:
```kotlin
package com.wolfpack.ui.nota

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wolfpack.data.model.Nota
import com.wolfpack.databinding.ItemNotaBinding
import java.text.SimpleDateFormat
import java.util.*

class NotaAdapter(
    private val materiaNames: Map<String, String>,
    private val onEdit: (Nota) -> Unit,
    private val onDelete: (Nota) -> Unit
) : ListAdapter<Nota, NotaAdapter.ViewHolder>(DIFF) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class ViewHolder(private val b: ItemNotaBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(nota: Nota) {
            b.tvTituloNota.text = nota.titulo
            b.tvMateriaChip.text = materiaNames[nota.materiaId] ?: "Sin materia"
            b.tvFechaModificacion.text = "Modificado: ${dateFormat.format(Date(nota.fechaModificacion))}"
            b.root.setOnClickListener { onEdit(nota) }
            b.btnDeleteNota.setOnClickListener { onDelete(nota) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemNotaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Nota>() {
            override fun areItemsTheSame(a: Nota, b: Nota) = a.uuid == b.uuid
            override fun areContentsTheSame(a: Nota, b: Nota) = a == b
        }
    }
}
```

- [ ] **Step 8: Crear NotaListActivity.kt**

`app/src/main/java/com/wolfpack/ui/nota/NotaListActivity.kt`:
```kotlin
package com.wolfpack.ui.nota

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.wolfpack.data.model.Nota
import com.wolfpack.databinding.ActivityNotaListBinding
import com.wolfpack.ui.base.BaseActivity
import com.wolfpack.ui.materia.MateriaListViewModel

class NotaListActivity : BaseActivity() {

    private lateinit var binding: ActivityNotaListBinding
    private val notaViewModel: NotaListViewModel by viewModels()
    private val materiaViewModel: MateriaListViewModel by viewModels()
    private lateinit var adapter: NotaAdapter
    private val userId get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun requiresAuth() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotaListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = NotaAdapter(emptyMap(), ::onEditNota, ::onDeleteNota)
        binding.rvNotas.layoutManager = LinearLayoutManager(this)
        binding.rvNotas.adapter = adapter

        binding.fabAddNota.setOnClickListener {
            startActivity(Intent(this, NotaFormActivity::class.java))
        }

        observeViewModels()
    }

    override fun onResume() {
        super.onResume()
        notaViewModel.loadNotas(userId)
        materiaViewModel.loadMaterias(userId)
    }

    private fun observeViewModels() {
        materiaViewModel.materias.observe(this) { materias ->
            val names = materias.associate { it.uuid to it.nombre }
            val currentList = notaViewModel.notas.value ?: emptyList()
            refreshAdapter(names, currentList)
        }
        notaViewModel.notas.observe(this) { list ->
            val names = materiaViewModel.materias.value?.associate { it.uuid to it.nombre } ?: emptyMap()
            refreshAdapter(names, list)
            binding.tvEmptyNotas.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        notaViewModel.loading.observe(this) { loading ->
            binding.progressBarNotas.visibility = if (loading) View.VISIBLE else View.GONE
        }
        notaViewModel.error.observe(this) { err ->
            err?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
        }
    }

    private fun refreshAdapter(names: Map<String, String>, list: List<Nota>) {
        adapter = NotaAdapter(names, ::onEditNota, ::onDeleteNota)
        binding.rvNotas.adapter = adapter
        adapter.submitList(list)
    }

    private fun onEditNota(nota: Nota) {
        startActivity(NotaFormActivity.editIntent(this, nota))
    }

    private fun onDeleteNota(nota: Nota) {
        notaViewModel.deleteNota(userId, nota.uuid)
    }
}
```

- [ ] **Step 9: Comando de commit para el usuario**

```bash
git add app/src/main/java/com/wolfpack/ui/nota/NotaListActivity.kt \
        app/src/main/java/com/wolfpack/ui/nota/NotaListViewModel.kt \
        app/src/main/java/com/wolfpack/ui/nota/NotaAdapter.kt \
        app/src/main/res/layout/activity_nota_list.xml \
        app/src/main/res/layout/item_nota.xml \
        app/src/test/java/com/wolfpack/ui/nota/NotaListViewModelTest.kt
git commit -m "funcionalidad: agregar pantalla lista de notas con RecyclerView"
```

---

## Task 7: Nota UI – Formulario

**Files:**
- Create: `app/src/main/java/com/wolfpack/ui/nota/NotaFormViewModel.kt`
- Create: `app/src/main/java/com/wolfpack/ui/nota/NotaFormActivity.kt`
- Create: `app/src/main/res/layout/activity_nota_form.xml`

- [ ] **Step 1: Crear activity_nota_form.xml**

`app/src/main/res/layout/activity_nota_form.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/background">

    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        app:title="Nueva Nota"
        app:navigationIcon="@drawable/ic_arrow_back" />

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="24dp">

            <com.google.android.material.textfield.TextInputLayout
                android:id="@+id/tilTitulo"
                style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="Título">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/etTitulo"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:inputType="textCapSentences" />
            </com.google.android.material.textfield.TextInputLayout>

            <com.google.android.material.textfield.TextInputLayout
                android:id="@+id/tilContenido"
                style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:hint="Contenido">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/etContenido"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:inputType="textMultiLine|textCapSentences"
                    android:minLines="4"
                    android:gravity="top" />
            </com.google.android.material.textfield.TextInputLayout>

            <com.google.android.material.textfield.TextInputLayout
                android:id="@+id/tilMateria"
                style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox.ExposedDropdownMenu"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:hint="Materia">

                <AutoCompleteTextView
                    android:id="@+id/actvMateria"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:inputType="none" />
            </com.google.android.material.textfield.TextInputLayout>

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btnGuardarNota"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="32dp"
                android:text="Guardar"
                app:cornerRadius="12dp" />
        </LinearLayout>
    </ScrollView>
</LinearLayout>
```

- [ ] **Step 2: Crear NotaFormViewModel.kt**

`app/src/main/java/com/wolfpack/ui/nota/NotaFormViewModel.kt`:
```kotlin
package com.wolfpack.ui.nota

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wolfpack.data.model.Materia
import com.wolfpack.data.model.Nota
import com.wolfpack.data.repository.MateriaRepository
import com.wolfpack.data.repository.NotaRepository
import kotlinx.coroutines.launch
import java.util.UUID

class NotaFormViewModel(
    private val notaRepo: NotaRepository = NotaRepository(),
    private val materiaRepo: MateriaRepository = MateriaRepository()
) : ViewModel() {

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _materias = MutableLiveData<List<Materia>>()
    val materias: LiveData<List<Materia>> = _materias

    fun loadMaterias(userId: String) {
        viewModelScope.launch {
            materiaRepo.getMaterias(userId)
                .onSuccess { list -> _materias.value = list.filter { it.activo } }
                .onFailure { _error.value = it.message }
        }
    }

    fun saveNota(
        userId: String,
        uuid: String?,
        titulo: String,
        contenido: String,
        materiaId: String,
        fechaCreacion: Long
    ) {
        if (titulo.isBlank()) {
            _error.value = "El título no puede estar vacío"
            return
        }
        _loading.value = true
        val now = System.currentTimeMillis()
        val nota = Nota(
            uuid = uuid ?: UUID.randomUUID().toString(),
            titulo = titulo.trim(),
            contenido = contenido.trim(),
            materiaId = materiaId,
            userId = userId,
            fechaCreacion = if (uuid == null) now else fechaCreacion,
            fechaModificacion = now
        )
        viewModelScope.launch {
            notaRepo.saveNota(userId, nota)
                .onSuccess { _saved.value = true }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }
}
```

- [ ] **Step 3: Crear NotaFormActivity.kt**

`app/src/main/java/com/wolfpack/ui/nota/NotaFormActivity.kt`:
```kotlin
package com.wolfpack.ui.nota

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.snackbar.Snackbar
import com.wolfpack.data.model.Nota
import com.wolfpack.databinding.ActivityNotaFormBinding
import com.wolfpack.ui.base.BaseActivity

class NotaFormActivity : BaseActivity() {

    private lateinit var binding: ActivityNotaFormBinding
    private val viewModel: NotaFormViewModel by viewModels()
    private val userId get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var editNota: Nota? = null
    private var selectedMateriaId: String = ""

    override fun requiresAuth() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotaFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        editNota = intent.getParcelableExtra(EXTRA_NOTA)
        editNota?.let { n ->
            binding.toolbar.title = "Editar Nota"
            binding.etTitulo.setText(n.titulo)
            binding.etContenido.setText(n.contenido)
        }

        binding.btnGuardarNota.setOnClickListener {
            viewModel.saveNota(
                userId = userId,
                uuid = editNota?.uuid,
                titulo = binding.etTitulo.text.toString(),
                contenido = binding.etContenido.text.toString(),
                materiaId = selectedMateriaId,
                fechaCreacion = editNota?.fechaCreacion ?: 0L
            )
        }

        viewModel.loadMaterias(userId)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.materias.observe(this) { materias ->
            val nombres = materias.map { it.nombre }
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nombres)
            binding.actvMateria.setAdapter(adapter)

            editNota?.let { n ->
                materias.find { it.uuid == n.materiaId }?.let { m ->
                    binding.actvMateria.setText(m.nombre, false)
                    selectedMateriaId = m.uuid
                }
            }

            binding.actvMateria.setOnItemClickListener { _, _, position, _ ->
                selectedMateriaId = materias[position].uuid
            }
        }

        viewModel.saved.observe(this) { if (it) finish() }

        viewModel.loading.observe(this) { loading ->
            binding.btnGuardarNota.isEnabled = !loading
        }

        viewModel.error.observe(this) { err ->
            err?.let {
                if (it.contains("título")) binding.tilTitulo.error = it
                else Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val EXTRA_NOTA = "extra_nota"

        fun editIntent(context: Context, nota: Nota): Intent =
            Intent(context, NotaFormActivity::class.java).putExtra(EXTRA_NOTA, nota)
    }
}
```

- [ ] **Step 4: Comando de commit para el usuario**

```bash
git add app/src/main/java/com/wolfpack/ui/nota/NotaFormActivity.kt \
        app/src/main/java/com/wolfpack/ui/nota/NotaFormViewModel.kt \
        app/src/main/res/layout/activity_nota_form.xml
git commit -m "funcionalidad: agregar formulario crear/editar nota con dropdown de materia"
```

---

## Task 8: HomeActivity – Navegación

**Files:**
- Modify: `app/src/main/res/layout/activity_home.xml`
- Modify: `app/src/main/java/com/wolfpack/ui/home/HomeActivity.kt`

- [ ] **Step 1: Reemplazar activity_home.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/background">

    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        app:title="NoteClass" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:orientation="vertical"
        android:gravity="center"
        android:padding="32dp">

        <TextView
            android:id="@+id/tvWelcome"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="22sp"
            android:textStyle="bold"
            android:textColor="@color/text_primary"
            android:layout_marginBottom="8dp" />

        <TextView
            android:id="@+id/tvEmail"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="14sp"
            android:textColor="@color/text_secondary"
            android:layout_marginBottom="40dp" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnVerNotas"
            android:layout_width="match_parent"
            android:layout_height="56dp"
            android:text="Mis Notas"
            android:layout_marginBottom="16dp"
            app:cornerRadius="12dp" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnVerMaterias"
            style="@style/Widget.MaterialComponents.Button.OutlinedButton"
            android:layout_width="match_parent"
            android:layout_height="56dp"
            android:text="Mis Materias"
            android:layout_marginBottom="40dp"
            app:cornerRadius="12dp" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnLogout"
            style="@style/Widget.MaterialComponents.Button.TextButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Cerrar sesión"
            android:textColor="@color/error" />
    </LinearLayout>
</LinearLayout>
```

- [ ] **Step 2: Actualizar HomeActivity.kt**

Leer el archivo actual y agregar en `onCreate` después de configurar los observers existentes:

```kotlin
binding.btnVerNotas.setOnClickListener {
    startActivity(Intent(this, com.wolfpack.ui.nota.NotaListActivity::class.java))
}
binding.btnVerMaterias.setOnClickListener {
    startActivity(Intent(this, com.wolfpack.ui.materia.MateriaListActivity::class.java))
}
```

Asegurarse que `tvWelcome` y `tvEmail` reciben datos del ViewModel existente.

- [ ] **Step 3: Comando de commit para el usuario**

```bash
git add app/src/main/res/layout/activity_home.xml \
        app/src/main/java/com/wolfpack/ui/home/HomeActivity.kt
git commit -m "funcionalidad: actualizar HomeActivity con navegación a notas y materias"
```

---

## Task 9: Manifest + Strings + ic_arrow_back

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/drawable/ic_arrow_back.xml`

- [ ] **Step 1: Registrar activities en AndroidManifest.xml**

Dentro de `<application>`, agregar:
```xml
<activity android:name=".ui.nota.NotaListActivity" />
<activity android:name=".ui.nota.NotaFormActivity" />
<activity android:name=".ui.materia.MateriaListActivity" />
<activity android:name=".ui.materia.MateriaFormActivity" />
```

- [ ] **Step 2: Crear ic_arrow_back.xml**

`app/src/main/res/drawable/ic_arrow_back.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorOnPrimary">
  <path
      android:fillColor="@android:color/white"
      android:pathData="M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z"/>
</vector>
```

- [ ] **Step 3: Agregar strings en strings.xml**

```xml
<string name="notas">Mis Notas</string>
<string name="materias">Mis Materias</string>
<string name="nueva_nota">Nueva Nota</string>
<string name="nueva_materia">Nueva Materia</string>
<string name="guardar">Guardar</string>
<string name="sin_notas">No tienes notas aún</string>
<string name="sin_materias">No tienes materias aún</string>
<string name="titulo">Título</string>
<string name="contenido">Contenido</string>
<string name="materia">Materia</string>
<string name="activo">Activa</string>
```

- [ ] **Step 4: Ejecutar todos los unit tests**

```
./gradlew :app:testDebugUnitTest
```
Esperado: Todos los tests PASSED.

- [ ] **Step 5: Comando de commit para el usuario**

```bash
git add app/src/main/AndroidManifest.xml \
        app/src/main/res/values/strings.xml \
        app/src/main/res/drawable/ic_arrow_back.xml
git commit -m "configuración: registrar activities, agregar strings e ícono de retroceso"
```