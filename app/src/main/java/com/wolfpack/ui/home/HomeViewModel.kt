package com.wolfpack.ui.home

import androidx.lifecycle.ViewModel
import com.wolfpack.data.repository.AuthRepository

class HomeViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    fun getCurrentUserEmail(): String = repo.currentUser?.email ?: "Usuario"

    fun logout() {
        repo.logout()
    }
}