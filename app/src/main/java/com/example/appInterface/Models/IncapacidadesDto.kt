package com.example.appinterface.Models

data class IncapacidadesDto(
    val idIncapacidad: Long? = null,
    val archivo: String,
    val estado: Int,
    val fechaInicio: String,
    val fechaFinal: String,
    val contratoId: Long
)
