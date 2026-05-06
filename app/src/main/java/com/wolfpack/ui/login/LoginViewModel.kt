package com.wolfpack.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wolfpack.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val repo: AuthRepository by lazy { AuthRepository() }

    private val _loginResult = MutableLiveData<Result<FirebaseUser>>()
    val loginResult: LiveData<Result<FirebaseUser>> = _loginResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun validateInputs(email: String, password: String): Boolean {
        return email.isNotBlank() && password.isNotBlank()
    }

    fun login(email: String, password: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = repo.loginWithEmail(email, password)
            _loginResult.value = result
            _loading.value = false
        }
    }

    fun loginWithGoogle(idToken: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = repo.loginWithGoogle(idToken)
            _loginResult.value = result
            _loading.value = false
        }
    }
}