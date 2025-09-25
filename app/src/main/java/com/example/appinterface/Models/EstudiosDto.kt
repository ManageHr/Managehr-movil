package com.example.appinterface.Models

data class EstudiosDto(
    val idEstudios: Long? = null,
    val nomEstudio: String,
    val nomInstitucion: String,
    val tituloObtenido: String,
    val anioInicio: String,
    val anioFinalizacion: String
)