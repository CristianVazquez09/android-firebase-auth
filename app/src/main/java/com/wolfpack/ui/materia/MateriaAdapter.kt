package com.wolfpack.ui.materia

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wolfpack.data.model.Materia
import com.wolfpack.databinding.ItemMateriaBinding

class MateriaAdapter(
    private val onToggleActivo: (Materia, Boolean) -> Unit,
    private val onDelete: (Materia) -> Unit,
    private val onEdit: (Materia) -> Unit
) : ListAdapter<Materia, MateriaAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemMateriaBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(materia: Materia) {
            b.tvNombreMateria.text = materia.nombre
            b.switchActivo.setOnCheckedChangeListener(null)
            b.switchActivo.isChecked = materia.activo
            b.switchActivo.setOnCheckedChangeListener { _, checked ->
                onToggleActivo(materia, checked)
            }
            b.btnDeleteMateria.setOnClickListener { onDelete(materia) }
            b.root.setOnClickListener { onEdit(materia) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemMateriaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Materia>() {
            override fun areItemsTheSame(a: Materia, b: Materia) = a.uuid == b.uuid
            override fun areContentsTheSame(a: Materia, b: Materia) = a == b
        }
    }
}