package com.wolfpack.ui.nota

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.wolfpack.data.model.Nota
import com.wolfpack.databinding.ActivityNotaFormBinding
import com.wolfpack.notifications.ReminderScheduler
import com.wolfpack.ui.base.BaseActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class NotaFormActivity : BaseActivity() {

    private lateinit var binding: ActivityNotaFormBinding
    private val viewModel: NotaFormViewModel by viewModels()
    private val userId get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    private var editNota: Nota? = null
    private var selectedMateriaId: String = ""
    private var pendingSavedNota: Nota? = null

    override fun requiresAuth() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotaFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupReminderUi()
        loadEditNota()

        binding.btnGuardarNota.setOnClickListener {
            saveNota()
        }

        viewModel.loadMaterias(userId)
        observeViewModel()
    }

    private fun setupReminderUi() {
        dateFormat.isLenient = false
        binding.npDia.minValue = 1
        binding.npMes.minValue = 1
        binding.npMes.maxValue = 12
        binding.npAnio.minValue = Calendar.getInstance().get(Calendar.YEAR)
        binding.npAnio.maxValue = Calendar.getInstance().get(Calendar.YEAR) + 5
        binding.npMes.displayedValues = arrayOf(
            "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
        )
        binding.tpHora.setIs24HourView(true)
        updateDayBounds()
        setReminderPickers(editNota?.fechaRecordatorio?.takeIf { it > 0L }
            ?: System.currentTimeMillis())

        binding.npMes.setOnValueChangedListener { _, _, _ -> updateDayBounds() }
        binding.npAnio.setOnValueChangedListener { _, _, _ -> updateDayBounds() }
        binding.switchRecordatorio.setOnCheckedChangeListener { _, checked ->
            binding.layoutRecordatorio.visibility = if (checked) View.VISIBLE else View.GONE
        }
    }

    private fun loadEditNota() {
        editNota = intent.getSerializableExtra(EXTRA_NOTA) as? Nota
        editNota?.let { n ->
            binding.toolbar.title = "Editar Nota"
            binding.etTitulo.setText(n.titulo)
            binding.etContenido.setText(n.contenido)
            selectedMateriaId = n.materiaId
            if (n.fechaRecordatorio > 0L) {
                binding.switchRecordatorio.isChecked = true
                setReminderPickers(n.fechaRecordatorio)
            }
        } ?: run {
            binding.toolbar.title = "Nueva Nota"
        }
    }

    private fun saveNota() {
        val now = System.currentTimeMillis()
        val uuid = editNota?.uuid ?: UUID.randomUUID().toString()
        val fechaRecordatorio = parseReminderDate() ?: return

        val nota = Nota(
            uuid = uuid,
            titulo = binding.etTitulo.text.toString().trim(),
            contenido = binding.etContenido.text.toString().trim(),
            materiaId = selectedMateriaId,
            userId = userId,
            fechaCreacion = editNota?.fechaCreacion ?: now,
            fechaModificacion = now,
            favorita = editNota?.favorita ?: false,
            fechaRecordatorio = fechaRecordatorio
        )

        pendingSavedNota = nota
        viewModel.saveNota(nota)
    }

    private fun parseReminderDate(): Long? {
        showReminderError(null)
        if (!binding.switchRecordatorio.isChecked) return 0L

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, binding.npAnio.value)
            set(Calendar.MONTH, binding.npMes.value - 1)
            set(Calendar.DAY_OF_MONTH, binding.npDia.value)
            set(Calendar.HOUR_OF_DAY, binding.tpHora.hour)
            set(Calendar.MINUTE, binding.tpHora.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            showReminderError("La fecha y hora deben ser futuras")
            return null
        }

        requestNotificationPermissionIfNeeded()
        return calendar.timeInMillis
    }

    private fun setReminderPickers(timestamp: Long) {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        binding.npAnio.value = calendar.get(Calendar.YEAR)
        binding.npMes.value = calendar.get(Calendar.MONTH) + 1
        updateDayBounds(calendar.get(Calendar.DAY_OF_MONTH))
        binding.tpHora.hour = calendar.get(Calendar.HOUR_OF_DAY)
        binding.tpHora.minute = calendar.get(Calendar.MINUTE)
    }

    private fun updateDayBounds(preferredDay: Int = binding.npDia.value) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, binding.npAnio.value)
            set(Calendar.MONTH, binding.npMes.value - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        binding.npDia.maxValue = maxDay
        binding.npDia.value = preferredDay.coerceIn(1, maxDay)
    }

    private fun showReminderError(message: String?) {
        binding.tvRecordatorioError.text = message.orEmpty()
        binding.tvRecordatorioError.visibility = if (message == null) View.GONE else View.VISIBLE
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

        viewModel.saved.observe(this) { saved ->
            if (saved) {
                pendingSavedNota?.let { nota ->
                    if (nota.fechaRecordatorio > 0L) {
                        ReminderScheduler.schedule(this, nota)
                    } else {
                        ReminderScheduler.cancel(this, nota.uuid)
                    }
                }
                finish()
            }
        }

        viewModel.loading.observe(this) { loading ->
            binding.btnGuardarNota.isEnabled = !loading
        }

        viewModel.error.observe(this) { err ->
            binding.tilTitulo.error = if (err?.contains("titulo") == true) err else null
            err?.let {
                if (!it.contains("titulo")) {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            REQUEST_NOTIFICATIONS
        )
    }

    companion object {
        private const val EXTRA_NOTA = "extra_nota"
        private const val REQUEST_NOTIFICATIONS = 1001

        fun editIntent(context: Context, nota: Nota): Intent =
            Intent(context, NotaFormActivity::class.java).putExtra(EXTRA_NOTA, nota)
    }
}
