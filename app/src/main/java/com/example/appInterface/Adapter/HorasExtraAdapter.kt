package com.example.appinterface.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Models.HorasExtraDto
import com.example.appinterface.R

class HorasExtraAdapter :
    ListAdapter<HorasExtraDto, HorasExtraAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<HorasExtraDto>() {
        override fun areItemsTheSame(old: HorasExtraDto, new: HorasExtraDto) =
            old.idHorasExtra == new.idHorasExtra

        override fun areContentsTheSame(old: HorasExtraDto, new: HorasExtraDto) =
            old == new
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFecha: TextView = itemView.findViewById(R.id.itemFecha)
        val tvDescripcion: TextView = itemView.findViewById(R.id.itemDescripcion)
        val tvCantidad: TextView = itemView.findViewById(R.id.itemCantidad)
        val tvTipo: TextView = itemView.findViewById(R.id.itemTipo)
        val tvEstado: TextView = itemView.findViewById(R.id.itemEstado)
        val tvContrato: TextView = itemView.findViewById(R.id.itemContrato)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_horas_extra, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val it = getItem(position)
        holder.tvFecha.text = it.fecha
        holder.tvDescripcion.text = it.descripcion
        holder.tvCantidad.text = "${it.nHorasExtra} h"
        holder.tvTipo.text = "Tipo: ${it.tipoHorasId}"
        holder.tvEstado.text = if (it.estado == 1) "Activo" else "Inactivo"
        holder.tvContrato.text = "Contrato: ${it.contratoId}"
    }
}
