package com.wolfpack.ui.forgotpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wolfpack.data.repository.AuthRepository
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _resetResult = MutableLiveData<Result<Unit>>()
    val resetResult: LiveData<Result<Unit>> = _resetResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _resetResult.value = Result.failure(Exception("El correo no puede estar vacío"))
            return
        }
        _loading.value = true
        viewModelScope.launch {
            _resetResult.value = repo.sendPasswordReset(email)
            _loading.value = false
        }
    }
}