package com.example.appinterface.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Models.ExperienciaDto
import com.example.appinterface.R

class ExperienciaAdapter : RecyclerView.Adapter<ExperienciaAdapter.ExperienciaViewHolder>() {

    private var experiencias: List<ExperienciaDto> = emptyList()

    class ExperienciaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmpresa: TextView = itemView.findViewById(R.id.tvEmpresa)
        val tvCargo: TextView = itemView.findViewById(R.id.tvCargo)
        val tvFechas: TextView = itemView.findViewById(R.id.tvFechas)
        val tvJefe: TextView = itemView.findViewById(R.id.tvJefe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperienciaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_experiencia, parent, false)
        return ExperienciaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExperienciaViewHolder, position: Int) {
        val experiencia = experiencias[position]
        holder.tvEmpresa.text = experiencia.nomEmpresa
        holder.tvCargo.text = experiencia.cargo
        holder.tvFechas.text = "${experiencia.fechaInicio} - ${experiencia.fechaFinalizacion}"
        holder.tvJefe.text = "Jefe: ${experiencia.nomJefe}"
    }

    override fun getItemCount(): Int = experiencias.size

    fun submitList(newList: List<ExperienciaDto>) {
        experiencias = newList
        notifyDataSetChanged()
        println("ExperienciaAdapter: ${newList.size} items cargados")
    }
}