package com.example.appinterface.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appinterface.Models.HojaDeVidaDto
import com.example.appinterface.R

class HojaDeVidaAdapter(
    private var hojasDeVida: List<HojaDeVidaDto> = emptyList(),
    private val onItemClick: (HojaDeVidaDto) -> Unit = {}
) : RecyclerView.Adapter<HojaDeVidaAdapter.HojaDeVidaViewHolder>() {

    private var hojas: List<HojaDeVidaDto> = emptyList()

    class HojaDeVidaViewHolder(itemView: View, private val onItemClick: (HojaDeVidaDto) -> Unit) : RecyclerView.ViewHolder(itemView) {
        val tvId: TextView = itemView.findViewById(R.id.tvId)
        val tvClaseLibreta: TextView = itemView.findViewById(R.id.tvClaseLibreta)
        val tvNumeroLibreta: TextView = itemView.findViewById(R.id.tvNumeroLibreta)
        val tvUsuarioNumDocumento: TextView = itemView.findViewById(R.id.tvUsuarioNumDocumento)

        fun bind(hoja: HojaDeVidaDto) {
            tvId.text = "ID: ${hoja.idHojaDeVida?.toString() ?: "N/A"}"
            tvClaseLibreta.text = "Clase: ${hoja.claseLibretaMilitar}"
            tvNumeroLibreta.text = "Número: ${hoja.numeroLibretaMilitar}"
            tvUsuarioNumDocumento.text = "Documento: ${hoja.usuarioNumDocumento}"

            // Agregar el clic aquí
            itemView.setOnClickListener {
                onItemClick(hoja)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HojaDeVidaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hojavida, parent, false)
        return HojaDeVidaViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: HojaDeVidaViewHolder, position: Int) {
        val hoja = hojas[position]
        holder.bind(hoja) // Usar el método bind en lugar de asignar directamente
    }

    override fun getItemCount(): Int = hojas.size

    fun submitList(newList: List<HojaDeVidaDto>) {
        hojas = newList
        notifyDataSetChanged()
    }
}