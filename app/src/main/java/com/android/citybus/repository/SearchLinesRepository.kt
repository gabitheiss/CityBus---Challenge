package com.android.citybus.repository

import com.android.citybus.domain.model.BusesLines
import com.android.citybus.domain.model.BusesPositionToLine
import com.android.citybus.service.BusesService
import retrofit2.Response

interface SearchLinesRepository {
    suspend fun getLines(searchTerm: String, cookie: String): Response<List<BusesLines>>
    suspend fun getBusesPositionToLine(line: String, cookie: String): Response<BusesPositionToLine>
    suspend fun toAuthenticate(): Response<Boolean>
}

class SearchLinesRepositoryImpl(private val service: BusesService) : SearchLinesRepository {

    override suspend fun getLines(searchTerm: String, cookie: String): Response<List<BusesLines>> {
        return service.getLines(searchTerm, cookie)
    }

    override suspend fun getBusesPositionToLine(line: String, cookie: String): Response<BusesPositionToLine> {
        return service.getBusesPositionToLine(line, cookie)
    }

    override suspend fun toAuthenticate(): Response<Boolean> {
        return service.toAuthenticate()
    }
}