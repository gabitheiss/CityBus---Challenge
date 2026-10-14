package com.android.citybus.service

import com.android.citybus.domain.model.BusesLines
import com.android.citybus.domain.model.BusesPosition
import com.android.citybus.domain.model.BusesPositionToLine
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BusesService {

    @GET("/v2.1/Posicao")
    suspend fun getBusesPosition(): Response<BusesPosition>

    @GET("/v2.1/Linha/Buscar")
    suspend fun getLines(
        @Query("termosBusca") searchTerm: String
    ): Response<List<BusesLines>>

    @GET("/v2.1/Posicao/Linha")
    suspend fun getBusesPositionToLine(
        @Query("codigoLinha") lineCode: String
    ): Response<BusesPositionToLine>
}
