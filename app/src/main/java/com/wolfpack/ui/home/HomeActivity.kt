package com.wolfpack.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.wolfpack.databinding.ActivityHomeBinding
import com.wolfpack.ui.base.BaseActivity
import com.wolfpack.ui.login.LoginActivity
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
        binding.tvUserEmail.text = viewModel.getCurrentUserEmail()

        val loginTime = tokenManager.getLoginTime()
        val expiresAt = loginTime + (3 * 60 * 60 * 1000L)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.tvSessionInfo.text = "Sesión expira a las ${sdf.format(Date(expiresAt))}"
    }

    private fun setupListeners() {
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