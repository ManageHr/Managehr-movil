package com.example.appInterface.Models

data class UsuarioDto (
    val numDocumento: Long,
    val primerNombre: String,
    val segundoNombre: String? = null,
    val primerApellido: String,
    val segundoApellido: String? = null,
    val password: String,
    val fechaNac: String,
    val numHijos: Int? = null,
    val contactoEmergencia: String,
    val numContactoEmergencia: String,
    val email: String,
    val direccion: String,
    val telefono: String,
    val nacionalidadId: Int,
    val epsCodigo: String,
    val generoId: Int,
    val tipoDocumentoId: Int,
    val estadoCivilId: Int,
    val pensionesCodigo: String,
    val usersId: Long
)
