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