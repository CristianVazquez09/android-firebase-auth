package com.wolfpack.ui.nota

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.wolfpack.data.model.Materia
import com.wolfpack.data.model.Nota
import com.wolfpack.databinding.ActivityNotaListBinding
import com.wolfpack.ui.base.BaseActivity
import com.wolfpack.ui.materia.MateriaListViewModel

class NotaListActivity : BaseActivity() {

    private lateinit var binding: ActivityNotaListBinding
    private val notaViewModel: NotaListViewModel by viewModels()
    private val materiaViewModel: MateriaListViewModel by viewModels()
    private lateinit var adapter: NotaAdapter
    private lateinit var materiaSummaryAdapter: MateriaNotaSummaryAdapter
    private val userId get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var selectedMateriaId: String? = null

    override fun requiresAuth() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotaListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = NotaAdapter(
            emptyMap(),
            emptyMap(),
            ::onEditNota,
            ::onDeleteNota,
            ::onToggleFavorita,
            ::onShareNota
        )
        binding.rvNotas.layoutManager = LinearLayoutManager(this)
        binding.rvNotas.adapter = adapter

        materiaSummaryAdapter = MateriaNotaSummaryAdapter { summary ->
            selectedMateriaId = summary.id
            notaViewModel.selectMateria(summary.id)
            updateSectionTitle()
            refreshMateriaCards()
        }
        binding.rvMateriaCards.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvMateriaCards.adapter = materiaSummaryAdapter

        binding.fabAddNota.setOnClickListener {
            startActivity(Intent(this, NotaFormActivity::class.java))
        }

        binding.etBuscarNotas.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                notaViewModel.searchNotas(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        binding.switchSoloFavoritas.setOnCheckedChangeListener { _, checked ->
            notaViewModel.setOnlyFavorites(checked)
        }

        observeViewModels()
    }

    override fun onResume() {
        super.onResume()
        notaViewModel.loadNotas(userId)
        materiaViewModel.loadMaterias(userId)
    }

    private fun observeViewModels() {
        materiaViewModel.materias.observe(this) { materias ->
            refreshAdapter(materias, notaViewModel.notas.value ?: emptyList())
            refreshMateriaCards()
        }
        notaViewModel.notas.observe(this) { list ->
            refreshAdapter(materiaViewModel.materias.value ?: emptyList(), list)
            binding.tvEmptyNotas.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        notaViewModel.allNotasLive.observe(this) {
            refreshMateriaCards()
        }
        notaViewModel.loading.observe(this) { loading ->
            binding.progressBarNotas.visibility = if (loading) View.VISIBLE else View.GONE
        }
        notaViewModel.error.observe(this) { err ->
            err?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
        }
    }

    private fun refreshAdapter(materias: List<Materia>, list: List<Nota>) {
        adapter.updateMaterias(
            names = materias.associate { it.uuid to it.nombre },
            colors = materias.associate { it.uuid to it.color },
            icons = materias.associate { it.uuid to it.icono }
        )
        adapter.submitList(list)
    }

    private fun refreshMateriaCards() {
        val materias = materiaViewModel.materias.value ?: emptyList()
        val allNotas = notaViewModel.allNotasLive.value ?: emptyList()
        val summaries = mutableListOf(
            MateriaNotaSummary(
                id = null,
                nombre = "Todas",
                color = "#25D07A",
                icono = "book",
                count = allNotas.size,
                selected = selectedMateriaId == null
            )
        )

        summaries += materias
            .filter { it.activo }
            .map { materia ->
                MateriaNotaSummary(
                    id = materia.uuid,
                    nombre = materia.nombre,
                    color = materia.color,
                    icono = materia.icono,
                    count = allNotas.count { it.materiaId == materia.uuid },
                    selected = selectedMateriaId == materia.uuid
                )
            }

        materiaSummaryAdapter.submitList(summaries)
    }

    private fun updateSectionTitle() {
        val selectedMateria = materiaViewModel.materias.value
            ?.firstOrNull { it.uuid == selectedMateriaId }
            ?.nombre
        binding.tvSeccionNotas.text = selectedMateria ?: "Todas las notas"
    }

    private fun onEditNota(nota: Nota) =
        startActivity(NotaFormActivity.editIntent(this, nota))

    private fun onDeleteNota(nota: Nota) =
        notaViewModel.deleteNota(userId, nota.uuid)

    private fun onToggleFavorita(nota: Nota) =
        notaViewModel.toggleFavorita(userId, nota)

    private fun onShareNota(nota: Nota) {
        val materia = materiaViewModel.materias.value
            ?.firstOrNull { it.uuid == nota.materiaId }
            ?.nombre
            ?: "Sin materia"
        val text = """
            ${nota.titulo}

            Materia: $materia

            ${nota.contenido}
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, nota.titulo)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Compartir nota"))
    }
}
