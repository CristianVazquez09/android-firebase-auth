package com.wolfpack.ui.nota

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wolfpack.R
import com.wolfpack.data.model.Nota
import com.wolfpack.databinding.ItemNotaBinding
import com.wolfpack.ui.materia.MateriaIcon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotaAdapter(
    materiaNames: Map<String, String>,
    materiaColors: Map<String, String>,
    private val onEdit: (Nota) -> Unit,
    private val onDelete: (Nota) -> Unit,
    private val onToggleFavorita: (Nota) -> Unit,
    private val onShare: (Nota) -> Unit
) : ListAdapter<Nota, NotaAdapter.ViewHolder>(DIFF) {

    private var materiaNames: Map<String, String> = materiaNames
    private var materiaColors: Map<String, String> = materiaColors
    private var materiaIcons: Map<String, String> = emptyMap()

    fun updateMaterias(
        names: Map<String, String>,
        colors: Map<String, String>,
        icons: Map<String, String>
    ) {
        materiaNames = names
        materiaColors = colors
        materiaIcons = icons
        notifyDataSetChanged()
    }

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class ViewHolder(private val b: ItemNotaBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(nota: Nota) {
            b.tvTituloNota.text = nota.titulo
            b.tvMateriaChip.text = materiaNames[nota.materiaId] ?: "Sin materia"
            b.tvFechaModificacion.text =
                "Modificado: ${dateFormat.format(Date(nota.fechaModificacion))}"

            val color = parseColor(materiaColors[nota.materiaId])
            b.viewMateriaColor.setBackgroundColor(color)
            b.tvMateriaChip.setTextColor(color)
            b.tvIconoNotaMateria.text = MateriaIcon.label(materiaIcons[nota.materiaId] ?: "book")
            b.tvIconoNotaMateria.setTextColor(color)

            b.btnFavoritaNota.setImageResource(
                if (nota.favorita) R.drawable.ic_star
                else R.drawable.ic_star_border
            )
            b.btnFavoritaNota.clearColorFilter()

            if (nota.fechaRecordatorio > 0L) {
                b.tvRecordatorio.visibility = View.VISIBLE
                b.tvRecordatorio.text =
                    "Recordatorio: ${dateFormat.format(Date(nota.fechaRecordatorio))}"
            } else {
                b.tvRecordatorio.visibility = View.GONE
            }

            b.root.setOnClickListener { onEdit(nota) }
            b.btnDeleteNota.setOnClickListener { onDelete(nota) }
            b.btnFavoritaNota.setOnClickListener { onToggleFavorita(nota) }
            b.btnCompartirNota.setOnClickListener { onShare(nota) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemNotaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    private fun parseColor(value: String?): Int = try {
        Color.parseColor(value ?: "#2196F3")
    } catch (_: IllegalArgumentException) {
        Color.parseColor("#2196F3")
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Nota>() {
            override fun areItemsTheSame(a: Nota, b: Nota) = a.uuid == b.uuid
            override fun areContentsTheSame(a: Nota, b: Nota) = a == b
        }
    }
}
