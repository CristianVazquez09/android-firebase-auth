package com.wolfpack.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wolfpack.data.model.UserProfile
import com.wolfpack.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _registerResult = MutableLiveData<Result<FirebaseUser>>()
    val registerResult: LiveData<Result<FirebaseUser>> = _registerResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun validateInputs(
        nombre: String,
        apellido: String,
        telefono: String,
        email: String,
        password: String
    ): Boolean {
        if (nombre.isBlank() || apellido.isBlank() || telefono.isBlank()) return false
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return false
        if (password.length < 6) return false
        return true
    }

    fun register(nombre: String, apellido: String, telefono: String, email: String, password: String) {
        _loading.value = true
        viewModelScope.launch {
            val profile = UserProfile(
                nombre = nombre,
                apellido = apellido,
                telefono = telefono,
                email = email
            )
            val result = repo.registerWithEmail(email, password, profile)
            _registerResult.value = result
            _loading.value = false
        }
    }
}