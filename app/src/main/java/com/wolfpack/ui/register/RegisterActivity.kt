package com.wolfpack.ui.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.wolfpack.R
import com.wolfpack.databinding.ActivityRegisterBinding
import com.wolfpack.ui.base.BaseActivity
import com.wolfpack.ui.home.HomeActivity

class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun requiresAuth(): Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !isLoading
        }

        viewModel.registerResult.observe(this) { result ->
            result.onSuccess { user ->
                user.getIdToken(false).addOnSuccessListener { tokenResult ->
                    tokenResult.token?.let { token ->
                        tokenManager.saveSession(token, user.uid)
                    }
                    goToHome()
                }
            }
            result.onFailure { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val apellido = binding.etApellido.text.toString().trim()
            val telefono = binding.etTelefono.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            when {
                !viewModel.validateInputs(nombre, apellido, telefono, email, password) -> {
                    Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
                }
                password.length < 6 -> {
                    binding.tilPassword.error = getString(R.string.error_password_short)
                }
                else -> {
                    binding.tilPassword.error = null
                    viewModel.register(nombre, apellido, telefono, email, password)
                }
            }
        }

        binding.tvGoToLogin.setOnClickListener { finish() }
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finishAffinity()
    }
}