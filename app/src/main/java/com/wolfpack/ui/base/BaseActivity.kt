package com.wolfpack.ui.base

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.wolfpack.data.local.TokenManager
import com.wolfpack.ui.login.LoginActivity

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenManager = TokenManager(this)
    }

    override fun onResume() {
        super.onResume()
        if (requiresAuth() && !tokenManager.isSessionValid()) {
            showSessionExpiredDialog()
        }
    }

    open fun requiresAuth(): Boolean = true

    private fun showSessionExpiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sesión expirada")
            .setMessage("Tu sesión ha expirado después de 3 horas. Por favor inicia sesión nuevamente.")
            .setCancelable(false)
            .setPositiveButton("Iniciar sesión") { _, _ ->
                tokenManager.clearSession()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .show()
    }
}