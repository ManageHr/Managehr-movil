package com.example.appinterface.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Models.EstudiosDto
import com.example.appinterface.R

class EstudiosAdapter : RecyclerView.Adapter<EstudiosAdapter.EstudiosViewHolder>() {

    private var estudios: List<EstudiosDto> = emptyList()

    class EstudiosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEstudio: TextView = itemView.findViewById(R.id.tvEstudio)
        val tvInstitucion: TextView = itemView.findViewById(R.id.tvInstitucion)
        val tvTitulo: TextView = itemView.findViewById(R.id.tvTitulo)
        val tvAnios: TextView = itemView.findViewById(R.id.tvAnios)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstudiosViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.iten_estudios, parent, false)
        return EstudiosViewHolder(view)
    }

    override fun onBindViewHolder(holder: EstudiosViewHolder, position: Int) {
        val estudio = estudios[position]
        holder.tvEstudio.text = estudio.nomEstudio
        holder.tvInstitucion.text = estudio.nomInstitucion
        holder.tvTitulo.text = estudio.tituloObtenido
        holder.tvAnios.text = "${estudio.anioInicio} - ${estudio.anioFinalizacion}"
    }

    override fun getItemCount(): Int = estudios.size

    fun submitList(newList: List<EstudiosDto>) {
        estudios = newList
        notifyDataSetChanged()
    }
}