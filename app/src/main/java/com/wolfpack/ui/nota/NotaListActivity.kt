package com.wolfpack.ui.nota

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.wolfpack.data.model.Nota
import com.wolfpack.databinding.ActivityNotaListBinding
import com.wolfpack.ui.base.BaseActivity
import com.wolfpack.ui.materia.MateriaListViewModel

class NotaListActivity : BaseActivity() {

    private lateinit var binding: ActivityNotaListBinding
    private val notaViewModel: NotaListViewModel by viewModels()
    private val materiaViewModel: MateriaListViewModel by viewModels()
    private lateinit var adapter: NotaAdapter
    private val userId get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun requiresAuth() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotaListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = NotaAdapter(emptyMap(), ::onEditNota, ::onDeleteNota)
        binding.rvNotas.layoutManager = LinearLayoutManager(this)
        binding.rvNotas.adapter = adapter

        binding.fabAddNota.setOnClickListener {
            startActivity(Intent(this, NotaFormActivity::class.java))
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
            val names = materias.associate { it.uuid to it.nombre }
            refreshAdapter(names, notaViewModel.notas.value ?: emptyList())
        }
        notaViewModel.notas.observe(this) { list ->
            val names = materiaViewModel.materias.value?.associate { it.uuid to it.nombre } ?: emptyMap()
            refreshAdapter(names, list)
            binding.tvEmptyNotas.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        notaViewModel.loading.observe(this) { loading ->
            binding.progressBarNotas.visibility = if (loading) View.VISIBLE else View.GONE
        }
        notaViewModel.error.observe(this) { err ->
            err?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
        }
    }

    private fun refreshAdapter(names: Map<String, String>, list: List<Nota>) {
        adapter.updateNames(names)
        adapter.submitList(list)
    }

    private fun onEditNota(nota: Nota) =
        startActivity(NotaFormActivity.editIntent(this, nota))

    private fun onDeleteNota(nota: Nota) =
        notaViewModel.deleteNota(userId, nota.uuid)
}