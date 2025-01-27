package com.android.citybus.service

import com.android.citybus.domain.model.BusesPosition
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface BusesService {

    @POST("/v2.1/Login/Autenticar")
    suspend fun toAuthenticate(@Query("token") token: String): Response<Boolean>

    @GET("/v2.1/Posicao")
    suspend fun getBusesPosition(
        @Query("token") token: String,
        @Header("Cookie") cookie: String?
    ): Response<BusesPosition>
}