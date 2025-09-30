package com.example.appinterface.Models

data class VacacionesDto(
    val idVacaciones: Long? = null,
    val motivo: String? = null,
    val fechaInicio: String,
    val fechaFinal: String,
    val contratoId: Int,
    val dias: Int? = null,
    val estado: String? = null
)
