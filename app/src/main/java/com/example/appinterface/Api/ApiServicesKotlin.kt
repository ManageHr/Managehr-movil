package com.example.appinterface.Api

import com.example.appinterface.Models.EstudiosDto
import com.example.appinterface.Models.ExperienciaDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiServicesKotlin {
    @GET("api/experiencia")
    fun obtenerExperiencias(): Call<List<ExperienciaDto>>

    @POST("api/experiencia")
    fun crearExperiencia(@Body experiencia: ExperienciaDto): Call<Void>

    @GET("api/estudios")
    fun obtenerEstudios(): Call<List<EstudiosDto>>

    @POST("api/estudios")
    fun crearEstudios(@Body estudios: EstudiosDto): Call<Void>

}