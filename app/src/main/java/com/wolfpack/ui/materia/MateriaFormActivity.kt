package com.wolfpack.ui.materia

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.wolfpack.data.model.Materia
import com.wolfpack.databinding.ActivityMateriaFormBinding
import com.wolfpack.ui.base.BaseActivity

class MateriaFormActivity : BaseActivity() {

    private lateinit var binding: ActivityMateriaFormBinding
    private val viewModel: MateriaFormViewModel by viewModels()
    private val userId get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var editMateria: Materia? = null

    override fun requiresAuth() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMateriaFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        editMateria = intent.getParcelableExtra(EXTRA_MATERIA)
        editMateria?.let { m ->
            binding.etNombreMateria.setText(m.nombre)
            binding.switchActivoForm.isChecked = m.activo
            binding.toolbar.title = "Editar Materia"
        } ?: run {
            binding.toolbar.title = "Nueva Materia"
        }

        binding.btnGuardarMateria.setOnClickListener {
            viewModel.saveMateria(
                userId = userId,
                uuid = editMateria?.uuid,
                nombre = binding.etNombreMateria.text.toString(),
                activo = binding.switchActivoForm.isChecked
            )
        }

        viewModel.saved.observe(this) { if (it) finish() }
        viewModel.error.observe(this) { err ->
            binding.tilNombreMateria.error = err
        }
    }

    companion object {
        private const val EXTRA_MATERIA = "extra_materia"

        fun editIntent(context: Context, materia: Materia): Intent =
            Intent(context, MateriaFormActivity::class.java).putExtra(EXTRA_MATERIA, materia)
    }
}