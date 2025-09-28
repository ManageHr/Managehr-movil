package com.example.appinterface.Models

data class HorasExtraDto(
    val idHorasExtra: Long? = null,
    val descripcion: String,
    val fecha: String,
    val tipoHorasId: Int,
    val nHorasExtra: Int,
    val estado: Int,
    val contratoId: Int
)

