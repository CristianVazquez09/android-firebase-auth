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

    fun saveMateria(
        userId: String,
        uuid: String?,
        nombre: String,
        activo: Boolean,
        color: String,
        icono: String
    ) {
        if (nombre.isBlank()) {
            _error.value = "El nombre no puede estar vacio"
            return
        }
        val materia = Materia(
            uuid = uuid ?: UUID.randomUUID().toString(),
            nombre = nombre.trim(),
            activo = activo,
            userId = userId,
            color = color,
            icono = icono
        )
        viewModelScope.launch {
            repo.saveMateria(userId, materia)
                .onSuccess { _saved.value = true }
                .onFailure { _error.value = it.message }
        }
    }
}
