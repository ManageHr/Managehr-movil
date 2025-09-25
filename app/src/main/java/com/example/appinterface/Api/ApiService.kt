package com.example.appinterface.Api

import com.example.appinterface.Models.DataResponse
import retrofit2.Call
import retrofit2.http.GET
interface ApiService {
    @GET("breed/hound/images")
    fun getHoundImages(): Call<DataResponse>

}