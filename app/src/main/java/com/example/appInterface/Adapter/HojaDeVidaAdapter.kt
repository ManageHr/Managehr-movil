package com.example.appinterface.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Models.HojaDeVidaDto
import com.example.appinterface.R

class HojaDeVidaAdapter : RecyclerView.Adapter<HojaDeVidaAdapter.HojaDeVidaViewHolder>() {

    private var hojas: List<HojaDeVidaDto> = emptyList()

    class HojaDeVidaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvId: TextView = itemView.findViewById(R.id.tvId)
        val tvClaseLibreta: TextView = itemView.findViewById(R.id.tvClaseLibreta)
        val tvNumeroLibreta: TextView = itemView.findViewById(R.id.tvNumeroLibreta)
        val tvUsuarioNumDocumento: TextView = itemView.findViewById(R.id.tvUsuarioNumDocumento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HojaDeVidaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hojavida, parent, false)
        return HojaDeVidaViewHolder(view)
    }

    override fun onBindViewHolder(holder: HojaDeVidaViewHolder, position: Int) {
        val hoja = hojas[position]
        holder.tvId.text = hoja.idHojaDeVida?.toString() ?: "N/A"
        holder.tvClaseLibreta.text = hoja.claseLibretaMilitar
        holder.tvNumeroLibreta.text = hoja.numeroLibretaMilitar
        holder.tvUsuarioNumDocumento.text = hoja.usuarioNumDocumento.toString()
    }

    override fun getItemCount(): Int = hojas.size

    fun submitList(newList: List<HojaDeVidaDto>) {
        hojas = newList
        notifyDataSetChanged()
    }
}