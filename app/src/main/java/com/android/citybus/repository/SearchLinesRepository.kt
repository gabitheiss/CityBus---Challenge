package com.android.citybus.repository

import com.android.citybus.domain.model.BusesLines
import com.android.citybus.service.BusesService
import retrofit2.Response


interface SearchLinesRepository {
    suspend fun getLines(token: String, searchTerm: String, cookie: String): Response<List<BusesLines>>
    suspend fun toAuthenticate(token: String): Response<Boolean>
}

class SearchLinesRepositoryImpl(private val service: BusesService) : SearchLinesRepository {

    override suspend fun getLines(token: String, searchTerm: String, cookie: String): Response<List<BusesLines>> {
        return service.getLines(token, searchTerm, cookie)
    }

    override suspend fun toAuthenticate(token: String): Response<Boolean> {
        return service.toAuthenticate(token)
    }
}