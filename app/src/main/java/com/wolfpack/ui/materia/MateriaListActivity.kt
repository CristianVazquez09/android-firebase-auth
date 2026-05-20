package com.wolfpack.ui.materia

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.wolfpack.databinding.ActivityMateriaListBinding
import com.wolfpack.ui.base.BaseActivity

class MateriaListActivity : BaseActivity() {

    private lateinit var binding: ActivityMateriaListBinding
    private val viewModel: MateriaListViewModel by viewModels()
    private lateinit var adapter: MateriaAdapter
    private val userId get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun requiresAuth() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMateriaListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = MateriaAdapter(
            onToggleActivo = { materia, checked ->
                viewModel.saveMateria(userId, materia.copy(activo = checked))
            },
            onDelete = { materia -> viewModel.deleteMateria(userId, materia.uuid) },
            onEdit = { materia ->
                startActivity(MateriaFormActivity.editIntent(this, materia))
            }
        )

        binding.rvMaterias.layoutManager = LinearLayoutManager(this)
        binding.rvMaterias.adapter = adapter
        binding.fabAddMateria.setOnClickListener {
            startActivity(Intent(this, MateriaFormActivity::class.java))
        }

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadMaterias(userId)
    }

    private fun observeViewModel() {
        viewModel.materias.observe(this) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.loading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(this) { err ->
            err?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
        }
    }
}