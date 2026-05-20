package com.wolfpack.ui.nota

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.wolfpack.data.model.Nota
import com.wolfpack.databinding.ActivityNotaFormBinding
import com.wolfpack.ui.base.BaseActivity

class NotaFormActivity : BaseActivity() {

    private lateinit var binding: ActivityNotaFormBinding
    private val viewModel: NotaFormViewModel by viewModels()
    private val userId get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var editNota: Nota? = null
    private var selectedMateriaId: String = ""

    override fun requiresAuth() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotaFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        editNota = intent.getParcelableExtra(EXTRA_NOTA)
        editNota?.let { n ->
            binding.toolbar.title = "Editar Nota"
            binding.etTitulo.setText(n.titulo)
            binding.etContenido.setText(n.contenido)
        } ?: run {
            binding.toolbar.title = "Nueva Nota"
        }

        binding.btnGuardarNota.setOnClickListener {
            viewModel.saveNota(
                userId = userId,
                uuid = editNota?.uuid,
                titulo = binding.etTitulo.text.toString(),
                contenido = binding.etContenido.text.toString(),
                materiaId = selectedMateriaId,
                fechaCreacion = editNota?.fechaCreacion ?: 0L
            )
        }

        viewModel.loadMaterias(userId)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.materias.observe(this) { materias ->
            val nombres = materias.map { it.nombre }
            val dropdownAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                nombres
            )
            binding.actvMateria.setAdapter(dropdownAdapter)

            editNota?.let { n ->
                materias.find { it.uuid == n.materiaId }?.let { m ->
                    binding.actvMateria.setText(m.nombre, false)
                    selectedMateriaId = m.uuid
                }
            }

            binding.actvMateria.setOnItemClickListener { _, _, position, _ ->
                selectedMateriaId = materias[position].uuid
            }
        }

        viewModel.saved.observe(this) { if (it) finish() }

        viewModel.loading.observe(this) { loading ->
            binding.btnGuardarNota.isEnabled = !loading
        }

        viewModel.error.observe(this) { err ->
            binding.tilTitulo.error = if (err?.contains("título") == true) err else null
            err?.let {
                if (!it.contains("título")) {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        private const val EXTRA_NOTA = "extra_nota"

        fun editIntent(context: Context, nota: Nota): Intent =
            Intent(context, NotaFormActivity::class.java).putExtra(EXTRA_NOTA, nota)
    }
}