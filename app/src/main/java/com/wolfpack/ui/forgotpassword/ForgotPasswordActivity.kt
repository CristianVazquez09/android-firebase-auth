package com.wolfpack.ui.forgotpassword

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.wolfpack.R
import com.wolfpack.databinding.ActivityForgotPasswordBinding
import com.wolfpack.ui.base.BaseActivity

class ForgotPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun requiresAuth(): Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnReset.isEnabled = !isLoading
        }

        viewModel.resetResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, getString(R.string.msg_reset_sent), Toast.LENGTH_LONG).show()
                finish()
            }
            result.onFailure { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnReset.setOnClickListener {
            viewModel.sendPasswordReset(binding.etEmail.text.toString().trim())
        }
        binding.tvBackToLogin.setOnClickListener { finish() }
    }
}