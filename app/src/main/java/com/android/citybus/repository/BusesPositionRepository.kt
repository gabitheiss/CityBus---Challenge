package com.android.citybus.repository

import com.android.citybus.domain.model.BusesPosition
import com.android.citybus.service.BusesService
import retrofit2.Response

interface BusesPositionRepository {
    suspend fun getBusesPosition(token: String, cookie: String): Response<BusesPosition>
    suspend fun toAuthenticate(token: String): Response<Boolean>
}

class BusesPositionRepositoryImpl(private val service: BusesService) : BusesPositionRepository {

    override suspend fun getBusesPosition(token: String, cookie: String): Response<BusesPosition> {
        return service.getBusesPosition(token, cookie)
    }

    override suspend fun toAuthenticate(token: String): Response<Boolean> {
        return service.toAuthenticate(token)
    }
}