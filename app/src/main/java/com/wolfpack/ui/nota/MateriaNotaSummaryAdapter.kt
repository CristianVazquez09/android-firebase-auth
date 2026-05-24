package com.wolfpack.ui.nota

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wolfpack.databinding.ItemMateriaNotaSummaryBinding
import com.wolfpack.ui.materia.MateriaIcon

data class MateriaNotaSummary(
    val id: String?,
    val nombre: String,
    val color: String,
    val icono: String,
    val count: Int,
    val selected: Boolean
)

class MateriaNotaSummaryAdapter(
    private val onClick: (MateriaNotaSummary) -> Unit
) : ListAdapter<MateriaNotaSummary, MateriaNotaSummaryAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemMateriaNotaSummaryBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(item: MateriaNotaSummary) {
            val color = parseColor(item.color)
            b.tvIconoMateriaResumen.text = MateriaIcon.label(item.icono)
            b.tvIconoMateriaResumen.setTextColor(color)
            b.tvNombreMateriaResumen.text = item.nombre
            b.tvConteoNotasResumen.text = "${item.count} ${if (item.count == 1) "nota" else "notas"}"
            b.root.strokeColor = if (item.selected) color else Color.TRANSPARENT
            b.root.strokeWidth = if (item.selected) 3 else 1
            b.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemMateriaNotaSummaryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    private fun parseColor(value: String): Int = try {
        Color.parseColor(value)
    } catch (_: IllegalArgumentException) {
        Color.parseColor("#2196F3")
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MateriaNotaSummary>() {
            override fun areItemsTheSame(a: MateriaNotaSummary, b: MateriaNotaSummary) =
                a.id == b.id

            override fun areContentsTheSame(a: MateriaNotaSummary, b: MateriaNotaSummary) =
                a == b
        }
    }
}
