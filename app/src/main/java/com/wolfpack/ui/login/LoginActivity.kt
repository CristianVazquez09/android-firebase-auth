package com.wolfpack.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.wolfpack.R
import com.wolfpack.databinding.ActivityLoginBinding
import com.wolfpack.ui.base.BaseActivity
import com.wolfpack.ui.forgotpassword.ForgotPasswordActivity
import com.wolfpack.ui.home.HomeActivity
import com.wolfpack.ui.register.RegisterActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    private val WEB_CLIENT_ID = "948768981588-ounrhsntafdfd3i3fih5llcu8thag4er.apps.googleusercontent.com"

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { viewModel.loginWithGoogle(it) }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google Sign-In falló (código ${e.statusCode}): ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun requiresAuth(): Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (tokenManager.isSessionValid()) {
            goToHome()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
            binding.btnGoogle.isEnabled = !isLoading
        }

        viewModel.loginResult.observe(this) { result ->
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
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            if (viewModel.validateInputs(email, password)) {
                viewModel.login(email, password)
            } else {
                Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGoogle.setOnClickListener { launchGoogleSignIn() }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}