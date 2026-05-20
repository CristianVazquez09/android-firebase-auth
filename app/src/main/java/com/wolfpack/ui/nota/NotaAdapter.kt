package com.wolfpack.ui.nota

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wolfpack.data.model.Nota
import com.wolfpack.databinding.ItemNotaBinding
import java.text.SimpleDateFormat
import java.util.*

class NotaAdapter(
    materiaNames: Map<String, String>,
    private val onEdit: (Nota) -> Unit,
    private val onDelete: (Nota) -> Unit
) : ListAdapter<Nota, NotaAdapter.ViewHolder>(DIFF) {

    var materiaNames: Map<String, String> = materiaNames
        private set

    fun updateNames(names: Map<String, String>) {
        materiaNames = names
        notifyDataSetChanged()
    }

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class ViewHolder(private val b: ItemNotaBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(nota: Nota) {
            b.tvTituloNota.text = nota.titulo
            b.tvMateriaChip.text = materiaNames[nota.materiaId] ?: "Sin materia"
            b.tvFechaModificacion.text = "Modificado: ${dateFormat.format(Date(nota.fechaModificacion))}"
            b.root.setOnClickListener { onEdit(nota) }
            b.btnDeleteNota.setOnClickListener { onDelete(nota) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemNotaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Nota>() {
            override fun areItemsTheSame(a: Nota, b: Nota) = a.uuid == b.uuid
            override fun areContentsTheSame(a: Nota, b: Nota) = a == b
        }
    }
}