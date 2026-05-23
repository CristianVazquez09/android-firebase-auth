package com.wolfpack.ui.materia

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
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
    private var selectedColor = DEFAULT_COLOR
    private var selectedIcon = DEFAULT_ICON

    override fun requiresAuth() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMateriaFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupColorDropdown()
        setupIconDropdown()

        editMateria = intent.getSerializableExtra(EXTRA_MATERIA) as? Materia
        editMateria?.let { m ->
            binding.etNombreMateria.setText(m.nombre)
            binding.switchActivoForm.isChecked = m.activo
            selectedColor = m.color
            selectedIcon = m.icono
            binding.actvColorMateria.setText(colorNameFor(m.color), false)
            binding.actvIconoMateria.setText(MateriaIcon.label(m.icono), false)
            binding.toolbar.title = "Editar Materia"
        } ?: run {
            binding.actvColorMateria.setText(colorNameFor(DEFAULT_COLOR), false)
            binding.actvIconoMateria.setText(MateriaIcon.label(DEFAULT_ICON), false)
            binding.toolbar.title = "Nueva Materia"
        }

        binding.btnGuardarMateria.setOnClickListener {
            viewModel.saveMateria(
                userId = userId,
                uuid = editMateria?.uuid,
                nombre = binding.etNombreMateria.text.toString(),
                activo = binding.switchActivoForm.isChecked,
                color = selectedColor,
                icono = selectedIcon
            )
        }

        viewModel.saved.observe(this) { if (it) finish() }
        viewModel.error.observe(this) { err ->
            binding.tilNombreMateria.error = err
        }
    }

    private fun setupIconDropdown() {
        val names = ICON_OPTIONS.values.toList()
        binding.actvIconoMateria.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names)
        )
        binding.actvIconoMateria.setOnItemClickListener { _, _, position, _ ->
            selectedIcon = names[position]
        }
    }

    private fun setupColorDropdown() {
        val names = COLOR_OPTIONS.keys.toList()
        binding.actvColorMateria.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names)
        )
        binding.actvColorMateria.setOnItemClickListener { _, _, position, _ ->
            selectedColor = COLOR_OPTIONS[names[position]] ?: DEFAULT_COLOR
        }
    }

    private fun colorNameFor(color: String): String =
        COLOR_OPTIONS.entries.firstOrNull { it.value.equals(color, ignoreCase = true) }?.key
            ?: "Azul"

    companion object {
        private const val EXTRA_MATERIA = "extra_materia"
        private const val DEFAULT_COLOR = "#2196F3"
        private const val DEFAULT_ICON = "📘"

        private val COLOR_OPTIONS = linkedMapOf(
            "Azul" to "#2196F3",
            "Verde" to "#4CAF50",
            "Rojo" to "#F44336",
            "Morado" to "#7E57C2",
            "Naranja" to "#FF9800",
            "Turquesa" to "#009688"
        )

        private val ICON_OPTIONS = linkedMapOf(
            "Libro" to "📘",
            "Libreta" to "📓",
            "Lapiz" to "✏️",
            "Calculadora" to "🧮",
            "Ciencia" to "🔬",
            "Historia" to "🏛️",
            "Arte" to "🎨",
            "Idioma" to "🌎",
            "Codigo" to "💻",
            "Examen" to "📝",
            "Idea" to "💡",
            "Favorita" to "⭐"
        )

        fun editIntent(context: Context, materia: Materia): Intent =
            Intent(context, MateriaFormActivity::class.java).putExtra(EXTRA_MATERIA, materia)
    }
}
