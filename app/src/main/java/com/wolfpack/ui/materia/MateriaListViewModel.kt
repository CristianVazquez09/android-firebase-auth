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