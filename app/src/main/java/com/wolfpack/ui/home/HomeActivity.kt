package com.wolfpack.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.wolfpack.databinding.ActivityHomeBinding
import com.wolfpack.ui.base.BaseActivity
import com.wolfpack.ui.login.LoginActivity
import com.wolfpack.ui.nota.NotaListActivity
import com.wolfpack.ui.materia.MateriaListActivity
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun requiresAuth(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        displayUserInfo()
        setupListeners()
    }

    private fun displayUserInfo() {
        val email = viewModel.getCurrentUserEmail()
        binding.tvWelcome.text = "¡Bienvenido!"
        binding.tvEmail.text = email
    }

    private fun setupListeners() {
        binding.btnVerNotas.setOnClickListener {
            startActivity(Intent(this, NotaListActivity::class.java))
        }
        binding.btnVerMaterias.setOnClickListener {
            startActivity(Intent(this, MateriaListActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            tokenManager.clearSession()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}