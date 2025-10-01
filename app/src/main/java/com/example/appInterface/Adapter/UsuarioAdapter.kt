package com.example.appinterface.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appInterface.Models.UsuarioDto


import com.example.appinterface.R

class UsuarioAdapter(
    private var usuarios: List<UsuarioDto> = emptyList(),
    private val onItemClick: (UsuarioDto) -> Unit = {}
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreCompleto: TextView = itemView.findViewById(R.id.tvNombreCompleto)
        val tvDocumento: TextView = itemView.findViewById(R.id.tvDocumento)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val tvTelefono: TextView = itemView.findViewById(R.id.tvTelefono)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuarios[position]
        val nombreCompleto = "${usuario.primerNombre} ${usuario.segundoNombre ?: ""} ${usuario.primerApellido} ${usuario.segundoApellido ?: ""}"

        holder.tvNombreCompleto.text = nombreCompleto.trim()
        holder.tvDocumento.text = "Documento: ${usuario.numDocumento}"
        holder.tvEmail.text = usuario.email
        holder.tvTelefono.text = "Tel: ${usuario.telefono}"

        holder.itemView.setOnClickListener {
            onItemClick(usuario)
        }
    }

    override fun getItemCount(): Int = usuarios.size

    fun submitList(newList: List<UsuarioDto>) {
        usuarios = newList
        notifyDataSetChanged()
        println("UsuarioAdapter: ${newList.size} usuarios cargados")
    }
}