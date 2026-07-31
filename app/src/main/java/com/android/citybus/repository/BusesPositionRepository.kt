package com.android.citybus.repository

import com.android.citybus.domain.model.BusesPosition
import com.android.citybus.service.BusesService
import retrofit2.Response

interface BusesPositionRepository {
    suspend fun getBusesPosition(cookie: String): Response<BusesPosition>
    suspend fun toAuthenticate(): Response<Boolean>
}

class BusesPositionRepositoryImpl(private val service: BusesService) : BusesPositionRepository {

    override suspend fun getBusesPosition(cookie: String): Response<BusesPosition> {
        return service.getBusesPosition(cookie)
    }

    override suspend fun toAuthenticate(): Response<Boolean> {
        return service.toAuthenticate()
    }
}