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

    fun saveNota(nota: Nota) {
        if (nota.titulo.isBlank()) {
            _error.value = "El titulo no puede estar vacio"
            return
        }
        _loading.value = true
        viewModelScope.launch {
            notaRepo.saveNota(nota.userId, nota)
                .onSuccess { _saved.value = true }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }
}
