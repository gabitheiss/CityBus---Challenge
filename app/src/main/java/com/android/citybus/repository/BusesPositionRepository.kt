package com.android.citybus.repository

import com.android.citybus.domain.model.BusesPosition
import com.android.citybus.service.BusesService
import retrofit2.Response

interface BusesPositionRepository {
    suspend fun getBusesPosition(): Response<BusesPosition>
}

class BusesPositionRepositoryImpl(private val service: BusesService) : BusesPositionRepository {

    override suspend fun getBusesPosition(): Response<BusesPosition> {
        return service.getBusesPosition()
    }
}
