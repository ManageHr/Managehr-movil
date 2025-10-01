// Adapter/VacacionesAdapter.kt
package com.example.appinterface.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Models.VacacionesDto
import com.example.appinterface.R

class VacacionesAdapter : RecyclerView.Adapter<VacacionesAdapter.VacacionViewHolder>() {

    private var items: List<VacacionesDto> = emptyList()

    class VacacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMotivo: TextView = itemView.findViewById(R.id.itemMotivo)
        val tvFecha: TextView = itemView.findViewById(R.id.itemFechas)
        val tvDias: TextView = itemView.findViewById(R.id.itemDias)
        val tvEstado: TextView = itemView.findViewById(R.id.itemEstado)
        val tvContrato: TextView = itemView.findViewById(R.id.itemContrato)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VacacionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vacaciones, parent, false)
        return VacacionViewHolder(view)
    }

    override fun onBindViewHolder(h: VacacionViewHolder, position: Int) {
        val v = items[position]
        h.tvMotivo.text   = v.motivo ?: "(sin motivo)"
        h.tvFecha.text    = "${v.fechaInicio} → ${v.fechaFinal}"
        h.tvDias.text     = "${v.dias ?: 0} días"
        h.tvEstado.text   = v.estado ?: "Pendiente"
        h.tvContrato.text = "Contrato: ${v.contratoId}"
    }

    override fun getItemCount(): Int = items.size

    fun submitList(list: List<VacacionesDto>) {
        items = list
        notifyDataSetChanged()
    }
}
