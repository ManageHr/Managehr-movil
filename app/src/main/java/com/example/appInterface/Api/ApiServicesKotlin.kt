package com.example.appinterface.Api

import com.example.appinterface.LoginActivity
import com.example.appinterface.Models.*
import retrofit2.Call
import retrofit2.http.*

interface ApiServicesKotlin {
    @GET("api/experiencia")
    fun obtenerExperiencias(): Call<List<ExperienciaDto>>

    @POST("api/experiencia")
    fun crearExperiencia(@Body experiencia: ExperienciaDto): Call<Void>

    @PUT("api/experiencia/{id}")
    fun actualizarExperiencia(@Path("id") id: Long, @Body experiencia: ExperienciaDto): Call<ExperienciaDto>

    @DELETE("api/experiencia/{id}")
    fun eliminarExperiencia(@Path("id") id: Long): Call<Void>

    @GET("api/estudios")
    fun obtenerEstudios(): Call<List<EstudiosDto>>

    @POST("api/estudios")
    fun crearEstudios(@Body estudios: EstudiosDto): Call<Void>

    @PUT("api/estudios/{id}")
    fun actualizarEstudio(@Path("id") id: Long, @Body estudio: EstudiosDto): Call<Void>

    @DELETE("api/estudios/{id}")
    fun eliminarEstudio(@Path("id") id: Long): Call<Void>

    @GET("api/hojas-de-vida")
    fun obtenerHojasDeVida(): Call<List<HojaDeVidaDto>>

    @POST("api/hojas-de-vida")
    fun crearHojasDeVida(@Body hojavida: HojaDeVidaDto): Call<Void>

    @PUT("api/hojas-de-vida/{id}")
    fun actualizarHojaDeVida(@Path("id") id: Long, @Body hojavida: HojaDeVidaDto): Call<Void>

    @DELETE("api/hojas-de-vida/{id}")
    fun eliminarHojaDeVida(@Path("id") id: Long): Call<Void>

    @POST("api/auth/login")
    fun login(@Body loginRequest: LoginActivity.LoginRequest): Call<LoginActivity.LoginResponse>

    @POST("api/horasextra")
    fun crearHorasExtra(@Body dto: HorasExtraDto): Call<Void>

    @GET("api/horasextra")
    fun obtenerHorasExtra(): Call<List<HorasExtraDto>>

    @POST("api/incapacidades")
    fun crearIncapacidad(@Body dto: IncapacidadesDto): Call<Void>

    @GET("api/incapacidades")
    fun obtenerIncapacidades(): Call<List<IncapacidadesDto>>

    @POST("api/vacaciones")
    fun crearVacaciones(@Body dto: VacacionesDto): Call<Void>

    @GET("api/vacaciones")
    fun obtenerVacaciones(): Call<List<VacacionesDto>>

}