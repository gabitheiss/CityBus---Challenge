package com.android.citybus.service

import com.android.citybus.domain.model.BusesLines
import com.android.citybus.domain.model.BusesPosition
import com.android.citybus.domain.model.BusesPositionToLine
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface BusesService {

    @POST("/v2.1/Login/Autenticar")
    suspend fun toAuthenticate(): Response<Boolean>

    @GET("/v2.1/Posicao")
    suspend fun getBusesPosition(
        @Header("Cookie") cookie: String?
    ): Response<BusesPosition>

    @GET("/v2.1/Linha/Buscar")
    suspend fun getLines(
        @Query("termosBusca") searchTerm: String,
        @Header("Cookie") cookie: String?
    ): Response<List<BusesLines>>

    @GET("/v2.1/Posicao/Linha")
    suspend fun getBusesPositionToLine(
        @Query("codigoLinha") lineCode: String,
        @Header("Cookie") cookie: String?
    ): Response<BusesPositionToLine>
}