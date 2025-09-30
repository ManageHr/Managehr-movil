package com.example.appinterface.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Models.EstudiosDto
import com.example.appinterface.R

class EstudiosAdapter(
    private var estudios: List<EstudiosDto> = emptyList(),
    private val onItemClick: (EstudiosDto) -> Unit = {}
) : RecyclerView.Adapter<EstudiosAdapter.EstudiosViewHolder>() {

    class EstudiosViewHolder(itemView: View, private val onItemClick: (EstudiosDto) -> Unit) : RecyclerView.ViewHolder(itemView) {
        val tvEstudio: TextView = itemView.findViewById(R.id.tvEstudio)
        val tvInstitucion: TextView = itemView.findViewById(R.id.tvInstitucion)
        val tvTitulo: TextView = itemView.findViewById(R.id.tvTitulo)
        val tvAnios: TextView = itemView.findViewById(R.id.tvAnios)

        fun bind(estudio: EstudiosDto) {
            tvEstudio.text = estudio.nomEstudio
            tvInstitucion.text = estudio.nomInstitucion
            tvTitulo.text = estudio.tituloObtenido
            tvAnios.text = "${estudio.anioInicio} - ${estudio.anioFinalizacion}"

            itemView.setOnClickListener {
                onItemClick(estudio)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstudiosViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.iten_estudios, parent, false)
        return EstudiosViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: EstudiosViewHolder, position: Int) {
        val estudio = estudios[position]
        holder.bind(estudio)
    }

    override fun getItemCount(): Int = estudios.size

    fun submitList(newList: List<EstudiosDto>) {
        estudios = newList
        notifyDataSetChanged()
    }
}