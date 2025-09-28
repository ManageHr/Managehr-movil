package com.example.appinterface.Models

data class ExperienciaDto(
    val idExperiencia: Long? = null,
    val nomEmpresa: String,
    val nomJefe: String,
    val telefono: String,
    val cargo: String,
    val actividades: String,
    val fechaInicio: String,
    val fechaFinalizacion: String
)