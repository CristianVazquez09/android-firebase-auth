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

    private val _allNotasLive = MutableLiveData<List<Nota>>()
    val allNotasLive: LiveData<List<Nota>> = _allNotasLive

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private var allNotas: List<Nota> = emptyList()
    private var searchQuery: String = ""
    private var onlyFavorites: Boolean = false
    private var selectedMateriaId: String? = null

    fun loadNotas(userId: String) {
        _loading.value = true
        viewModelScope.launch {
            repo.getNotas(userId)
                .onSuccess { list ->
                    allNotas = list.sortedByDescending { it.fechaModificacion }
                    _allNotasLive.value = allNotas
                    applyFilters()
                }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun searchNotas(query: String) {
        searchQuery = query.trim()
        applyFilters()
    }

    fun setOnlyFavorites(enabled: Boolean) {
        onlyFavorites = enabled
        applyFilters()
    }

    fun selectMateria(materiaId: String?) {
        selectedMateriaId = materiaId
        applyFilters()
    }

    fun toggleFavorita(userId: String, nota: Nota) {
        val newValue = !nota.favorita
        viewModelScope.launch {
            repo.updateFavorita(userId, nota.uuid, newValue)
                .onSuccess {
                    allNotas = allNotas.map {
                        if (it.uuid == nota.uuid) it.copy(favorita = newValue) else it
                    }
                    _allNotasLive.value = allNotas
                    applyFilters()
                }
                .onFailure { _error.value = it.message }
        }
    }

    fun deleteNota(userId: String, uuid: String) {
        viewModelScope.launch {
            repo.deleteNota(userId, uuid)
                .onSuccess { loadNotas(userId) }
                .onFailure { _error.value = it.message }
        }
    }

    private fun applyFilters() {
        val filtered = allNotas
            .filter { nota ->
                searchQuery.isBlank() ||
                    nota.titulo.contains(searchQuery, ignoreCase = true) ||
                    nota.contenido.contains(searchQuery, ignoreCase = true)
            }
            .filter { nota -> selectedMateriaId == null || nota.materiaId == selectedMateriaId }
            .filter { nota -> !onlyFavorites || nota.favorita }
            .sortedWith(compareByDescending<Nota> { it.favorita }.thenByDescending { it.fechaModificacion })

        _notas.value = filtered
    }
}
