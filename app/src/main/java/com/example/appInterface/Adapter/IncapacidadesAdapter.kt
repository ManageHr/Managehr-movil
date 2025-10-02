package com.example.appinterface.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Models.IncapacidadesDto
import com.example.appinterface.R

class IncapacidadesAdapter(
    private val incapacidadesList: List<IncapacidadesDto>,
    private val onItemClick: (IncapacidadesDto) -> Unit
) : RecyclerView.Adapter<IncapacidadesAdapter.IncViewHolder>() {

    private var items: List<IncapacidadesDto> = emptyList()

    class IncViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvArchivo: TextView = itemView.findViewById(R.id.itemArchivo)
        val tvEstado: TextView = itemView.findViewById(R.id.itemEstado)
        val tvFecha: TextView = itemView.findViewById(R.id.itemFechas)
        val tvContrato: TextView = itemView.findViewById(R.id.itemContrato)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_incapacidades, parent, false)
        return IncViewHolder(v)
    }

    override fun onBindViewHolder(h: IncViewHolder, position: Int) {
        val it = items[position]
        h.tvArchivo.text = "Archivo: ${it.archivo}"
        h.tvEstado.text = if (it.estado == 1) "Aprobado" else "Pendiente"
        h.tvFecha.text = "Del ${it.fechaInicio} al ${it.fechaFinal}"
        h.tvContrato.text = "Contrato: ${it.contratoId}"
        h.itemView.setOnClickListener { _ ->
            onItemClick(it)
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(list: List<IncapacidadesDto>) {
        items = list
        notifyDataSetChanged()
    }
}
